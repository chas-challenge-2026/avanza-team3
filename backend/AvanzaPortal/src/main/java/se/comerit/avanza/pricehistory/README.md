# Historiska priser

Skelett med samma lager som andra backendpaket: controller, dto, model,
repository och service, samt provider för den externa integrationen.
Ingen koppling till frontend, riskberäkning eller schemalagd import ännu.

## Användning

Flyway V6 skapar `instrument_prices` vid nästa vanliga applikationsstart.
Alla endpoints kräver inloggning via befintlig JWT-konfiguration.

```http
GET /api/price-history/instruments/1?from=2025-01-01&to=2025-12-31
POST /api/price-history/instruments/1/import?from=2025-01-01&to=2025-12-31
```

GET läser bara databasen. POST importerar från vald provider och returnerar
lagrad historik för intervallet. Båda datumgränserna ingår. Priser sorteras
äldst först. Återimport uppdaterar befintliga datum och behåller övriga dagar.
En tom lista betyder att inga lagrade priser finns i intervallet; det är ingen
garanti för att hela intervallet har hämtats. Saknade handelsdagar fylls inte ut.

Standardprovidern `stub` returnerar en tom lista. Lägg egna fixtures eller
CSV-läsning i `StubHistoricalPriceProvider` om utvecklingsdata behövs.
Inga slumpade kurser används. `configuredProvider` i svaret anger vald
implementation, inte ursprung för varje lagrad rad. Ursprung lagras i `source`.

## Koppla in det riktiga API:t

1. Implementera `ApiHistoricalPriceProvider.fetchPrices`. Injicera HTTP-klient
   och konfiguration för bas-URL, credentials och timeouts. Förvara hemligheter
   utanför källkoden.
2. Mappa instrument till leverantörens symbol och börs inne i adaptern.
   Mappa därefter externa svar till `HistoricalPricePoint`. Hämta alla sidor
   om leverantören paginerar.
3. Använd dagliga slutkurser i instrumentets valuta (versaler). `adjustedClose`
   är nullable; sätt aldrig automatiskt samma värde som `close` om API:t saknar
   justerade priser. Definiera leverantörens behandling av splittar/utdelningar.
   Priser måste vara positiva och rymmas i DECIMAL(24,8); normalisera eventuell
   högre precision uttryckligen i adaptern.
4. Kasta `HistoricalPriceProviderException` vid transportfel, rate limiting
   eller okänd symbol. En lyckad fråga utan observationer returnerar tom lista.
   Providerfel ger HTTP 503; felaktigt datumintervall 400; saknat instrument 404.
5. Byt `source()` till leverantörens stabila namn (max 50 tecken) och sätt
   `market.history.provider=api` i application.properties eller miljövariabeln
   `MARKET_HISTORY_PROVIDER=api`. Utan implementation returnerar API-läget 503.

Endast provider-adaptern och dess anslutningsinställningar behöver ändras
för att ansluta datakällan. Controller, databas och service använder eget kontrakt.

## Avgränsningar för skelettet

Importen är synkron och håller en transaktion under hämtningen. Kör inte
överlappande importer för samma instrument samtidigt: unikhetsvillkoret skyddar
mot dubbletter men en samtidig import kan misslyckas och behöva köras igen.
Inför schemalagd/storskalig drift: separera nätverksanrop från skrivtransaktionen,
begränsa intervall och importfrekvens och lägg till jobbhantering/låsning.
Import är i dag tillgänglig för inloggade användare enligt projektets befintliga
säkerhetsregler. Historiska FX-kurser och transaktioner ingår inte.

Verifiering: `mvn -Dtest=HistoricalPriceServiceTest,HistoricalPriceControllerTest test` (Java 21).
