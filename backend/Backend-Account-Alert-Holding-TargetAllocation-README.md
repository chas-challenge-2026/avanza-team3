# Backend v2 – Account, Holding, Alert, Target Allocation och Rebalancing

Det här dokumentet uppdaterar den tidigare sammanställningen av Account-, Holding-, Alert- och TargetAllocation-API:erna. Det beskriver **aktuell backendimplementation i v2**, inte de ursprungliga v1-controllers eller de gamla `seed.sql`-tabellerna. Se även [v2-arkitekturen](../docs/v2/architecture.md) och [projekt-README](../README.md).

**Swagger/OpenAPI är referens för de aktuella HTTP-kontrakten:**

- Lokal Swagger UI: `http://localhost:8082/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8082/v3/api-docs`
- Källkod: `backend/AvanzaPortal/src/main/java/se/comerit/avanza/`

## Teknik och struktur

- **Java 21 / Spring Boot 3.2.11**, Spring Web, Spring Security, JPA/Hibernate, PostgreSQL 15 och Flyway.
- Controllers tar emot request-DTO:er och returnerar response-DTO:er. Servicelagret ansvarar för behörighet, transaktioner, validering och beräkningar. Repositories hanterar databasfrågor.
- **JWT Bearer** används i stället för v1:s `HttpSession`. `@EnableMethodSecurity` och `@PreAuthorize` finns. Vid konto-/innehavsåtkomst filtreras också på autentiserad användares ID.
- DTO-validering (`@Valid`, storleks-, tal- och obligatoriska fält) kombineras med affärsregler i tjänster.
- `Pageable` används på centrala listendpoints, vanligtvis `page=0&size=20` som standard och högst 100 per sida.
- Läsningar kan cachas; skrivande Holding-/Rebalance-flöden använder cacheinvalidering. Mutationer som hör ihop ligger i transactional-block.

## Endpointöversikt

Alla `/api/**`-anrop kräver i normalfallet `Authorization: Bearer <JWT>`, utom inloggning. För detaljerade fält och felkoder: se Swagger.

### Authentication

| Metod | Endpoint | Funktion |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Autentisera och returnera JWT |
| `DELETE` | `/api/auth/logout` | Svarar med utloggningsbekräftelse; spärrar **inte** JWT server-side |
| `GET` | `/api/auth/me` | Returnerar inloggad användares uppgifter |

`AuthService` använder `UserRepository` (JPA) och BCrypt. `JwtService` signerar token och kontrollerar giltighetstid (standard 24 timmar). JWT-nyckel anges genom `JWT_SECRET`.

### Account – CRUD

| Metod | Endpoint | Funktion |
| --- | --- | --- |
| `GET` | `/api/accounts?page=0&size=20` | Paginerad lista över egna konton |
| `GET` | `/api/accounts/{accountId}` | Läs ett eget konto |
| `POST` | `/api/accounts` | Skapa konto |
| `PUT` | `/api/accounts/{accountId}` | Ersätt redigerbara kontofält |
| `PATCH` | `/api/accounts/{accountId}` | Uppdatera angivna fält |
| `DELETE` | `/api/accounts/{accountId}` | Ta bort tomt konto |

`AccountService` kontrollerar ägarskap före läsning och ändring. Ett konto med innehav eller positivt `cash_balance_sek` får inte raderas. Flera konton av samma typ stöds. Kontot har också `rebalance_version` för rebalanseringsflödet.

### Holding – CRUD, filtrering och värdering

| Metod | Endpoint | Funktion |
| --- | --- | --- |
| `GET` | `/api/holdings?page=0&size=20&accountId=1&instrumentType=STOCK` | Paginerad, filtrerbar lista |
| `GET` | `/api/holdings/summary` | Komplett filtrerat innehavsurval för sammanfattningar, **utan pagination** |
| `GET` | `/api/holdings/{holdingId}` | Hämta eget innehav |
| `POST` | `/api/holdings` | Skapa innehav; `accountId` anges i request-body |
| `PATCH` | `/api/holdings/{holdingId}` | Ändra bara skickade fält |
| `DELETE` | `/api/holdings/{holdingId}` | Radera eget innehav |

**Notera ändringen jämfört med äldre dokumentation:** `POST /api/accounts/{accountId}/holdings` är inte den aktuella create-endpointen. Det är `POST /api/holdings` med exempelvis `accountId`, `ticker`, `quantity` och `avgBuyPrice` i requesten.

`HoldingService` verifierar konto- och innehavsägarskap. Instrument hämtas från instrumentkatalogen; marknadspris, marknadsvärde och orealiserat resultat beräknas via backendtjänster. Cacherensning görs vid mutationer.

### Instruments och Market Data

| Metod | Endpoint | Funktion |
| --- | --- | --- |
| `GET` | `/api/instruments` | Lista tillgängliga instrument |
| `GET` | `/api/instruments/{instrumentId}` | Instrumentdetaljer |
| `POST` | `/api/instruments` | Skapa instrument |
| `GET` | `/api/market/price/{ticker}` | Pris via marknadsdataprovider |
| `GET` | `/api/market/fx/{from}/{to}` | Växelkurs |
| `GET` | `/api/market/fx/convert?amount=100&from=USD&to=SEK` | Valutakonvertering |

`MarketDataService` väljer data från `MarketDataProvider`-implementationer. Native FX via JNA finns, men `FallbackMarketDataProvider` använder fasta demo-priser och kurser. För okänd ticker returneras i dag ett generellt fallbackpris på **100,00**. Flyway V9 lägger till instrument och prisrader i databasen men **runtime-prisinhämtningen läser inte automatiskt `instrument_prices`**. Detta ska inte förväxlas med livehandel.

### Target allocations – två separata modeller

**Äldre mål per kontotyp**, finns kvar för befintliga funktioner:

| Metod | Endpoint | Funktion |
| --- | --- | --- |
| `GET` | `/api/target-allocations?page=0&size=20` | Paginerad lista |
| `GET` | `/api/target-allocations/{targetAllocationId}` | Hämta ett mål |
| `POST` | `/api/target-allocations` | Skapa mål per kontotyp |
| `PATCH` | `/api/target-allocations/{targetAllocationId}` | Ändra mål |
| `DELETE` | `/api/target-allocations/{targetAllocationId}` | Ta bort mål |

**Nya mål per individuellt konto**, används i live-notiser och rebalansering:

| Metod | Endpoint | Funktion |
| --- | --- | --- |
| `GET` | `/api/accounts/{accountId}/allocation-targets` | Läs kontots mål |
| `PUT` | `/api/accounts/{accountId}/allocation-targets` | Ersätt hela målallokeringen; summan måste vara 100 % |
| `DELETE` | `/api/accounts/{accountId}/allocation-targets` | Ta bort kontots mål |
| `GET` | `/api/accounts/{accountId}/allocation` | Faktisk allokering, mål och differenser |

Request-exempel för `PUT /api/accounts/{accountId}/allocation-targets`:

```json
{
  "targets": [
    { "category": "STOCK", "targetPercentage": 60.00 },
    { "category": "FUND", "targetPercentage": 40.00 }
  ]
}
```

Kategorierna är `STOCK`, `FUND`, `BOND`, `OTHER`. `ETF` klassas som `FUND`, och tillgängligt kontantsaldo ingår i `OTHER` vid beräkning av faktisk allokering. `AccountAllocationTargetService` kräver unika kategorier och exakt 100 % total. `AccountAllocationOverviewService` räknar ut faktiska andelar och avvikelser.

### Alert – sparade och dynamiska notiser

| Metod | Endpoint | Funktion |
| --- | --- | --- |
| `GET` | `/api/alerts?dismissed=false&page=0&size=20` | Paginerade sparade notiser |
| `PATCH` | `/api/alerts/{alertId}/dismiss` | Avfärda en egen sparad notis |
| `GET` | `/api/alerts/live` | Beräkna kontobaserade live-notiser på begäran |

`AlertService` använder `AccountAllocationOverviewService` för **alla användarens konton** och skapar `LIVE_DRIFT` om `STOCK` eller `FUND` avviker **mer än fem procentenheter** från kontots mål. Gränsen definieras i `AllocationRules.DRIFT_THRESHOLD_PERCENTAGE_POINTS`. Live-notiser lagras inte i databasen och ska inte förväxlas med sparade/dismissbara alerts.

**API-kontrakt att verifiera mot klienten:** nuvarande `LiveAlertDto` har JSON-fälten `alertType` och `createdAt` (camelCase). En kontrollerad frontendtyp har använt `alert_type` och `created_at`. Lös detta tillsammans med frontendgruppen; beskriv inte ett format som inte överensstämmer med koden.

### Rebalancing – simulering per konto

| Metod | Endpoint | Funktion |
| --- | --- | --- |
| `POST` | `/api/accounts/{accountId}/rebalance/preview` | Beräkna förslag; inga databasskrivningar |
| `POST` | `/api/accounts/{accountId}/rebalance/confirm` | Genomför simulerade ändringar i innehav och kontantsaldo |

**Preview-request:**

```json
{ "allowedInstrumentIds": [1, 6] }
```

Detta är ID:n för instrument användaren tillåter i förslaget. En preview förutsätter kontomål som summerar till 100 % och returnerar bland annat:

- `accountId`, `proposalToken`, `executable`, `warnings`
- `totalValueSek`, `cashBeforeSek`, `cashAfterSek`
- `before`, `after` och `trades`
- `proposedHoldings` – **hela föreslagna innehavslistan**, inklusive oförändrade och helt sålda innehav (`currentQuantity` respektive `proposedQuantity`)

**Confirm-request:**

```json
{ "proposalToken": "<token from preview>" }
```

Confirm returnerar bland annat `status: "EXECUTED"`, `cashBalanceSek` och `executedTrades`. Det betyder en **simulerad genomförd ändring i projektets PostgreSQL-databas**, inte riktig handel.

`RebalanceCalculator` beräknar köp/sälj separat från databaslagret. `RebalanceService` verifierar ägarskap och styr transaktioner. `RebalanceProposalTokenService` skapar signerade HMAC-token med giltighetstid **10 minuter**. Vid confirm kontrolleras kontolås, `rebalance_version`, innehavs-/pris-/målsnapshot samt token innan uppdatering. Preview ska vara läsande och får inte ändra innehav.

### Dashboard och risk

| Metod | Endpoint | Funktion |
| --- | --- | --- |
| `GET` | `/api/portfolio?page=0&size=20` | Dashboard/portföljsammanställning |
| `GET` | `/api/risk/portfolio` | Riskmått och historik |

Riskvärden beräknas genom `RiskService` och C/JNA. `PortfolioRiskService` använder fortfarande **simulerad historik** (`MOCKED`), vilket begränsar hur siffrorna kan tolkas.

## Databas, migration och transaktioner

Aktuellt schema finns i Flywayfiler under `AvanzaPortal/src/main/resources/db/migration/`; äldre SQL-utdrag från `infra/seed.sql` är inte längre en korrekt beskrivning av hela datamodellen.

- V1–V3: ursprungligt schema, seedning och lösenordsmigrering.
- V4–V6: instrumentmodell, klassificering och prislagring.
- V7: målallokering per enskilt konto.
- V8: konto-kassasaldo och versionsnummer för rebalansering.
- V9: demofonder, obligation, råvaru-ETF och simulerade prisrader.

Rebalanseringens `confirm` är transaktionell och kan uppdatera flera holdings och konto i samma operation. Vanliga Holding API-ändringar behöver testas mot samtidiga rebalanseringsflöden innan systemet kan betraktas som produktionssäkert.

## Testning och verifiering

Kör tester lokalt från `backend/AvanzaPortal`:

```bash
mvn test
```

CI-jobbet `backend-tests` kör Maven-tester vid PR och push mot `develop` eller `main`. Exempel på testområden i repot är `HoldingServiceTest`, `AccountAllocationOverviewServiceTest`, `RebalanceServiceTest`, `RebalanceProposalTokenServiceTest`, `MarketDataServiceTest` och `NativeFxMarketDataProviderTest`.

Enhetstesterna för rebalansering omfattar exempelvis läsande preview, confirm en gång, avvisning av upprepad bekräftelse och avvisning när kassasaldot ändrats. Alla tester eller 100 % kodtäckning kan inte antas vara verifierade enbart utifrån denna dokumentation; kontrollera CI-resultat och utför nödvändiga integrationstester med PostgreSQL och Docker.

## Kända begränsningar och nästa steg

1. Instrumentprisers fallback är simulerad, inklusive ett generellt standardpris för okänd ticker. Runtime-provider läser inte V9:s `instrument_prices` automatiskt.
2. Riskhistorik är mockad trots en fungerande JNA-beräkningsmodul.
3. JWT återkallas inte vid logout och databasens demouppgifter finns i konfigurationen.
4. JSON-kontraktet för `LiveAlertDto` måste samordnas med klientens förväntningar.
5. Samtidighet mellan vanligt Holding API och rebalansering samt fullständiga databas- och end-to-end-tester behöver fortsatt uppföljning.

För systemövergripande dokumentation relevant för projektet finns: [docs/v2/known-bugs.md](../docs/v2/known-bugs.md), [docs/v2/README-pain-points.md](../docs/v2/README-pain-points.md) och [docs/v2/v2-targets.md](../docs/v2/v2-targets.md).


## Säkerhetstest – OWASP ZAP

En OWASP ZAP-skanning har genomförts som underlag för bedömningen av projektets säkerhet.

Skanningen identifierade en risk med allvarlighetsgraden **High**, direkt kopplad till Swagger/OpenAPI. Som åtgärd har Swagger inaktiverats i deploymiljön. Swagger finns fortfarande tillgängligt vid lokal utveckling och testning.

Den identifierade exponeringen är inte tillgänglig i deploy, detta har verifierats.

Sedan den senaste skanningen har ytterligare funktionalitet och endpoints lagts till. Därför bör en ny OWASP ZAP-skanning genomföras om tiden tillåter före slutleveransen.

Den tidigare skanningen kan alltså inte betraktas som en fullständig säkerhetsverifiering av den slutliga versionen.

**OWASP ZAP-rapporten bifogas i projektets rotkatalog** som dokumentation av genomförd säkerhetstestning.


## Prestandamätning med k6

### Testupplägg

Prestandatester genomfördes den 3 november 2026 med **k6** för att verifiera projektkravet på **P95 under 200 ms vid 100 samtidiga användare och 10 000 innehav**.

Testerna kördes mot en isolerad lokal Docker-miljö med Spring Boot och PostgreSQL 15. Testdatabasen innehöll 10 000 syntetiska innehav kopplade till ett testkonto.

Följande endpoints testades:

- `GET /api/holdings?page=0&size=20`
- `GET /api/portfolio?page=0&size=100`

Belastningstestet kördes med **100 virtuella användare (VUs) under 60 sekunder**. Rate limiting inaktiverades enbart i den isolerade testmiljön för att kunna mäta backendens svarstider utan att anrop blockerades med HTTP 429.

### Identifierat prestandaproblem

De inledande testerna visade att `/api/portfolio` överskred kravet på P95 under 200 ms.

Undersökningen visade att dashboarden hämtade och värderade samtliga 10 000 innehav individuellt vid varje anrop, trots att själva innehavslistan var paginerad.

Tidsmätningar i `DashboardService` visade att en stor del av bearbetningstiden användes av `PortfolioValuationService`. Vid hög samtidig belastning ökade dessutom svarstiderna kraftigt.

Även en ökning av HikariCP:s anslutningspool från 10 till 30 testades, men gav sämre prestanda. Poolstorleken återställdes därför till 10.

### Genomförd optimering

Backend optimerades genom att flytta delar av sammanställningen till databaslagret.

I stället för att hämta och värdera alla individuella innehav för dashboardens sammanställningar grupperas innehav i databasen. Java utför sedan värderingar och allokeringsberäkningar utifrån de aggregerade resultaten.

Den befintliga paginationen och API:ets JSON-struktur behölls. Inga frontendändringar krävdes.

### Resultat före och efter optimering

Jämförbara tester genomfördes med 100 VUs under 60 sekunder och `size=100` för portföljendpointen.

| Mätvärde | Före optimering | Efter optimering |
|---|---:|---:|
| `/api/holdings` P95 | 20,06 ms | **5,64 ms** |
| `/api/portfolio` P95 | 669,71 ms | **33,25 ms** |
| HTTP-fel | 0 % | **0 %** |
| Genomförda iterationer | 4 628 | **5 748** |

Efter optimeringen minskade P95 för `/api/portfolio` med cirka **95 %**. Båda testade endpoints uppfyllde därmed prestandakravet på under 200 ms i testmiljön.

Totalt genomfördes 11 496 lyckade kontroller vid det avslutande lasttestet, utan HTTP-fel.

### Verifiering och begränsningar

Efter kodändringarna kördes backendtesterna med Java 21 och Maven:

- **205 tester totalt**
- **200 godkända**
- **0 fel**
- **5 överhoppade**
- **BUILD SUCCESS**

Lasttestresultaten har följande begränsningar:

- De 100 virtuella användarna använde samma testkonto och JWT.
- De 10 000 testinnehaven bestod nästan helt av samma instrument.
- Rate limiting var inaktiverat under prestandamätningen.
- Testerna utfördes lokalt och är inte ett bevis på motsvarande prestanda i produktionsmiljö.

Resultaten verifierar därför prestandakravet **under de beskrivna testförutsättningarna**, men ersätter inte kompletterande tester med flera separata användare och mer varierade innehav.

### Testunderlag

Filerna finns i `infra/`:

- `k6-load.js` – lasttestskript.
- `k6-summary-size100-100vu.json` – resultat före optimering.
- `k6-summary-optimized-100vu.json` – resultat efter optimering.

Testet kördes med:

```powershell
k6 run --vus 100 --duration 60s --summary-export=k6-summary-optimized-100vu.json .\k6-load.js
```

### AI-stöd

k6 var ett nytt verktyg för mig. Testerna och verifieringen genomfördes manuellt, med omfattande AI-stöd för testupplägg, felsökning, analys och framtagning av kodoptimeringen.