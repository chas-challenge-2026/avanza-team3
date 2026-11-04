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


Backendens tester har genomförts och servicemetodernas testtäckning har granskats och bedömts som tillräcklig.

Vid den dokumenterade Maven-körningen den 3 november 2026 redovisades 205 tester: 200 godkända, 0 fel och 5 överhoppade. De överhoppade testerna ingår inte i antalet godkända tester.

PostgreSQL-integrationstestet `RebalancePostgresIntegrationTest` har dessutom körts separat mot PostgreSQL och godkänts. Testet verifierar bland annat preview, confirm och uppdatering av innehav. Applikationen är också driftsatt och har funktionstestats av teamet.

## Kända begränsningar och nästa steg

1. Instrumentprisers fallback är simulerad, inklusive ett generellt standardpris för okänd ticker. Runtime-provider läser inte V9:s `instrument_prices` automatiskt.
2. Riskhistorik är mockad trots en fungerande JNA-beräkningsmodul.
3. JWT återkallas inte vid logout och databasens demouppgifter finns i konfigurationen.
4. Backendens tester och PostgreSQL-integrationstestet för rebalansering har genomförts och godkänts. Ytterligare riktade tester av samtidiga ändringar via Holding API och rebalansering kan vara relevanta inför produktionsanvändning.


## Säkerhetstest – OWASP ZAP

En OWASP ZAP-skanning har genomförts som underlag för bedömningen av projektets säkerhet.

Skanningen identifierade en risk med allvarlighetsgraden **High**, direkt kopplad till Swagger/OpenAPI. Som åtgärd har Swagger inaktiverats i deploymiljön. Swagger finns fortfarande tillgängligt vid lokal utveckling och testning.

Den identifierade exponeringen är inte tillgänglig i deploy, detta har verifierats.

Sedan den senaste skanningen har ytterligare funktionalitet och endpoints lagts till. Därför bör en ny OWASP ZAP-skanning genomföras om tiden tillåter före slutleveransen.

Den tidigare skanningen kan alltså inte betraktas som en fullständig säkerhetsverifiering av den slutliga versionen.

[OWASP ZAP-rapport – 19 oktober 2026](../2026-10-19-ZAP-Report3-.html)


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



## Bearbetning av specialisternas feedforward

Under projektets gång har vi fått återkoppling från bland annat CTO och specialister inom användarupplevelse. Återkopplingen har använts för att prioritera säkerhetsåtgärder, identifiera tekniska brister och diskutera förbättringar av användarupplevelsen.

### Säkerhet och backend – CTO

CTO lyfte bland annat behovet av bättre backendvalidering, central felhantering, skydd mot exponering av stacktraces, säkrare API:er och automatiserade säkerhetskontroller.

**Backendvalidering:** En stor del av valideringen fanns redan, men efter återkopplingen gick jag igenom implementationen och kompletterade vissa luckor. Valideringen omfattar bland annat tillåtna värden, format och affärsregler.

**Felhantering och stacktraces:** Jag arbetade med att införa `GlobalExceptionHandler` för centraliserad hantering av API-fel och standardiserade felmeddelanden. Jag konfigurerade också applikationen så att interna stacktraces inte skickas tillbaka till klienten.

**Rate limiting:** Jag implementerade begränsningar av antalet anrop till inloggning och övriga API-endpoints som ytterligare skydd mot överbelastning och upprepade anrop.

**Säkerhetsskanning:** Jag genomförde en OWASP ZAP-skanning. Den identifierade bland annat ett High-fynd kopplat till Swagger/OpenAPI. Som åtgärd inaktiverades Swagger i deploymiljön, och vi verifierade att exponeringen inte längre fanns där. Eftersom applikationen har vidareutvecklats efter skanningen behövs en ny skanning för att verifiera slutversionen.

Projektet använder även Spring Security, JWT-autentisering och ägarskapskontroller för att skydda användardata. Detta ingår i teamets övergripande säkerhetsarbete.

### CI/CD och beroendehantering

GitHub Actions används för automatiserade backendtester och frontendkontroller.

Dependabot har lagts till och skapar uppdateringsförslag via pull requests. Dessa har dock inte hanterats konsekvent, vilket innebär att identifierade beroendeuppdateringar kan bli liggande.

Automatiserad sårbarhetsskanning med exempelvis `npm audit` som en del av CI/CD har implementerats.

### UUID – medveten avgränsning

CTO rekommenderade att ersätta sekventiella databas-ID:n med UUID för att göra identifierare svårare att förutsäga.

Jag tog på mig att undersöka och genomföra ändringen, men valde att prioritera andra åtgärder inom den återstående projekttiden.

En UUID-migrering skulle beröra databasrelationer, entiteter, API-kontrakt, tester och eventuellt frontend. Den är därför inte genomförd och kvarstår som en medveten avgränsning.

UUID ersätter inte behovet av ägarskaps- och behörighetskontroller, som fortsatt är avgörande för att skydda användarnas uppgifter.

### Användarupplevelse – feedback till frontendgruppen

Feedbacken om användarupplevelsen betonade behovet av en tydlig helhetsbild av sparandet, begripliga riskmått och färre svårtolkade grafer.

Specialisterna lyfte även vikten av att förklara begrepp som målallokering och diversifiering samt att tydliggöra var informationen kommer ifrån.

Denna återkoppling hanterades huvudsakligen av frontendgruppen genom deras arbete med design och presentation av portföljinformationen.

### Kvarstående brister och vidareutveckling

All feedforward har inte kunnat genomföras inom projektperioden. Följande områden kvarstår:

- **UUID:** Sekventiella ID:n har inte ersatts.
- **Audit trail och loggskydd:** Fullständig spårbarhet för ändringar och skydd mot manipulation av loggar har inte implementerats.
- **Aktiv övervakning:** Automatisk övervakning och larm vid driftstörningar är inte färdigställda.
- **Beroendeuppdateringar:** Dependabot finns, men inkommande pull requests behöver granskas och hanteras.
- **Automatiserad säkerhetsskanning:** Ytterligare säkerhetskontroller i CI/CD återstår.
- **Marknadsdata och fallback:** Vid saknade priser kan applikationen använda simulerade eller generiska standardpriser. Det riskerar att ge missvisande portföljvärden och ska inte betraktas som en säker fallbacklösning.
- **Återhämtning vid driftstörningar:** Mer fullständig hantering av tillfälliga tjänstefel och återförsök återstår.

Dessa begränsningar behöver dokumenteras tydligt så att det framgår vad systemet faktiskt klarar och vad som kräver vidareutveckling.


## Köra PostgreSQL-integrationstest för rebalansering

`RebalancePostgresIntegrationTest` testar rebalanseringsflödet mot en riktig PostgreSQL-databas, inklusive förhandsgranskning, bekräftelse, uppdatering av innehav och kontoavvikelser.

Testet är normalt avaktiverat och aktiveras med miljövariabeln `REBALANCE_PG_TEST=true`. Testet har körts och verifierats.

### Förutsättningar

- Docker Desktop är installerat och igång.
- Java och Maven är installerade.
- Kommandona körs i Windows PowerShell.
- En separat PostgreSQL-databas används för testet.

### 1. Starta PostgreSQL med Docker

Kör följande kommando för att skapa och starta en testdatabas:

```powershell
docker run -d --name avanza-rebalance-test -e POSTGRES_DB=avanza -e POSTGRES_USER=avanza -e POSTGRES_PASSWORD=avanza123 -p 15432:5432 postgres:15
```

Detta behöver endast göras första gången.

Om containern redan finns men är stoppad, använd i stället:

```powershell
docker start avanza-rebalance-test
```

### 2. Kontrollera att databasen körs

```powershell
docker ps
```

Kontrollera att `avanza-rebalance-test` har status `Up`.

Kontrollera sedan att PostgreSQL accepterar anslutningar:

```powershell
docker exec avanza-rebalance-test pg_isready -U avanza -d avanza
```

Förväntat svar:

```text
/var/run/postgresql:5432 - accepting connections
```

### 3. Navigera till backend

Från projektets rotmapp:

```powershell
cd backend/AvanzaPortal
```

### 4. Konfigurera miljövariabler

Kör följande kommandon i PowerShell:

```powershell
$env:REBALANCE_PG_TEST="true"

$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:15432/avanza"
$env:SPRING_DATASOURCE_USERNAME="avanza"
$env:SPRING_DATASOURCE_PASSWORD="avanza123"

$env:SPRING_FLYWAY_URL="jdbc:postgresql://localhost:15432/avanza"
$env:SPRING_FLYWAY_USER="avanza"
$env:SPRING_FLYWAY_PASSWORD="avanza123"

$env:JWT_SECRET="test-secret-key-with-at-least-32-characters"
```

Dessa värden gäller för den lokala testmiljön och ska inte användas i produktion.

### 5. Kör integrationstestet

```powershell
mvn "-Dtest=RebalancePostgresIntegrationTest" test
```

### 6. Förväntat resultat

Om testet lyckas visas:

```text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

Flyway applicerar automatiskt de databasmigreringar som ännu inte har körts. Vid första lyckade testkörningen applicerades migrationerna V1–V9.

### Felsökning

**Testet skippas**

Kontrollera att miljövariabeln är satt:

```powershell
$env:REBALANCE_PG_TEST="true"
```

**Connection refused**

Databasen körs inte eller är inte tillgänglig på port `15432`. Starta containern:

```powershell
docker start avanza-rebalance-test
```

**Password authentication failed**

Kontrollera att både `SPRING_DATASOURCE_*` och `SPRING_FLYWAY_*` använder rätt användarnamn och lösenord.

**BUILD FAILURE**

Läs felmeddelandet i terminalen eller kontrollera testrapporterna under:

```text
backend/AvanzaPortal/target/surefire-reports/
```

### Stoppa testdatabasen

När testerna är klara kan databasen stoppas med:

```powershell
docker stop avanza-rebalance-test
```

Den kan sedan startas igen med `docker start avanza-rebalance-test` utan att behöva skapas på nytt.