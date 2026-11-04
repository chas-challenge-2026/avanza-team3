# Beslutslogg – Avanza Portal v2

**Projekt:** Avanza Team 3  
**Sammanställd:** 4 november 2026  
**Underlag:** projektanteckningar, genomförda ändringar och pull requests i [teamets GitHub-repo](https://github.com/chas-challenge-2026/avanza-team3), tester och konversationer på måndagar, onsdagar samt tisdagsmöten med PL.


## Genomförda tekniska och organisatoriska vägval

### Beslut om namngivning av branscher
- **När:** Start: Vecka 2-3
- **Beslut/vägval:** Vi kom överens i ett gruppmöte om att branscher ska namnges efter issue nummer samt vad man gör i branschen.
- **Varför:**  För att ge alla bättre översikt och skapa struktur och uppmuntra folk till att skapa issues på github.
- **Konsekvens/status:** Alla fick bättre översikt över vem som jobbade med vad.
- **Stöd:** anteckningar, samt namngivning av branscher som finns kvar.

### Stegvis refaktorering till separata lager
- **När:** Start: Vecka 2-3
- **Beslut/vägval:** Flytta logik från v1:s controllers till separata controllers, services och repositories. Genomföra migreringen stegvis i stället för att skriva om allt samtidigt. Tilda börjar med Auth, Kostas med Holding, Felix med Dashboard, den som blir klar först tar Alert därefter.
- **Varför:** Den gamla kodbasen blandade HTTP, affärslogik och databasåtkomst. I anteckningarna framgår en vilja att begränsa stora ändringar och minska merge-konflikter. Vi kom överens om en struktur som byggde på paketnamn samt ansvarsfördelning.
- **Konsekvens/status:** Auth och Holding började delas upp direkt; därefter har strukturen införts i fler backenddelar. Temporära JDBC-delar ersattes successivt.
- **Stöd:** [AuthService](https://github.com/chas-challenge-2026/avanza-team3/commit/689d79fa), [Holding – stegvis migration](https://github.com/chas-challenge-2026/avanza-team3/commit/b3578906).

### Uppgradera teknikstacken till Java 21, Spring Boot 3 och PostgreSQL 15
- **När:** Start: Vecka 2-3
- **Beslut/vägval:** Modernisera den äldre Java/Spring- och databasmiljön enligt v2-målet. Tilda uppdaterar till Java 21 och senare Spring Boot. Kostas uppdaterar Postgres.
- **Varför:** Skapa en enhetlig och mer underhållbar miljö för JPA, säkerhet, tester och Docker.
- **Konsekvens/status:** Projektet använder Java 21, Spring Boot 3.2.11 och PostgreSQL 15. Databasuppgraderingen krävde även hantering av Flyway- och schemaskillnader.
- **Stöd:** [Uppgradera Java/Spring](https://github.com/chas-challenge-2026/avanza-team3/commit/34bb5496), [PostgreSQL 15-arbete](https://github.com/chas-challenge-2026/avanza-team3/commit/ad4e1ea1).

### Beslut om endpoints
- **När:** Start: Vecka 2-4
- **Beslut/vägval:** Vi byggde endpoints ursprungligen baserat på instruktioner i v2-targets.md 
- **Varför:**  För att göra det tydligt vad vi bygger och varför så försökte vi följa instruktioner som givits så nogrannt som möjligt.
- **Konsekvens/status:** Alla hade tolkat uppgiften lite olika, så det ledde till viss förvirring i start då det inte fanns motsvarande instruktioner för tex Frontend.
- **Stöd:** Md filer som kom med projektet.

### Införa pagination, men separat fullständig sammanställning vid behov
- **När:** Vecka 2-4
- **Beslut/vägval:** Paginerade REST-listor för konton, innehav och notiser; separat `GET /api/holdings/summary` för de tillfällen då hela det filtrerade innehavsurvalet behövs.
- **Varför:** Undvika stora listresultat för vanlig navigering utan att förlora möjligheten att räkna samman hela portföljen.
- **Konsekvens/status:** Både paginerade och opaginerade API:er finns. Att fullständiga sammanställningar också behöver optimeras upptäcktes senare i k6-testningen.
- **Stöd:** [Pagination Holding](https://github.com/chas-challenge-2026/avanza-team3/commit/d2b83a0f), [Summary-endpoint](https://github.com/chas-challenge-2026/avanza-team3/commit/92f571ee).

### Ersätta sessionshantering och MD5 med Spring Security, BCrypt och JWT
- **När:** Vecka 2-4
- **Beslut/vägval:** Införa signerade JWT-token och Spring Security för REST-API:t i stället för v1:s `HttpSession`; använda BCrypt för lösenord.
- **Varför:** Förbättra autentisering och möjliggöra en separat React-klient.
- **Konsekvens/status:** Inloggnings- och behörighetsflödet har migrerats. JWT återkallas fortfarande inte server-side vid logout.
- **Stöd:** [JwtService](https://github.com/chas-challenge-2026/avanza-team3/commit/0e21b6cf), [SecurityConfig](https://github.com/chas-challenge-2026/avanza-team3/commit/4308d1a5), [Frontend/auth PR #153](https://github.com/chas-challenge-2026/avanza-team3/pull/153).

### C/C++ och Java ska kopplas genom JNA brygga
- **När:** Augusti–oktober 2026.
- **Beslut/vägval:** Felix som jobbat på att bryta ut dashboard-controller ska samarbeta med C studenter för att skapa brygga mellan Java och C.
- **Varför:** Uppfylla v2:s native-krav och separera beräkningskomponenterna från HTTP- och databaskoden.
- **Konsekvens/status:** Riskmått och FX-bryggor finns i projektet. Riskhistoriken som används för vissa resultat är dock simulerad.
- **Stöd:** [Riskmodul](https://github.com/chas-challenge-2026/avanza-team3/commit/7ab10b91), [JNA-merge](https://github.com/chas-challenge-2026/avanza-team3/commit/0fcbcb9f), [Native FX/risk-endpoints](https://github.com/chas-challenge-2026/avanza-team3/commit/88170eab).

### Separat instrumentkatalog i stället för duplicerade instrumentuppgifter
- **När:** 30 september–14 oktober 2026.
- **Beslut/vägval:**  Kostas ska skapa en egen Instrument-entitet med ticker, namn, sektor, typ och valuta samt koppla innehav till instrument.
- **Varför:** Kunna återanvända korrekt instrumentinformation och stödja filtrering, allokering och presentation.
- **Konsekvens/status:** Instrumentkatalog och migrationsstöd finns; frontendens fullständiga användning av katalogen behöver verifieras.
- **Stöd:** [Instrumentmodell/Flyway](https://github.com/chas-challenge-2026/avanza-team3/commit/8673782d), [Koppling till Holding PR #278](https://github.com/chas-challenge-2026/avanza-team3/pull/278).

### Införa pull requests, CI-kontroller och Dependabot
- **När:** 5–7 oktober 2026.
- **Beslut/vägval:** Arbeta via pull requests och kodgranskning för integrationsgrenen, köra automatiserade backend-/frontendkontroller och låta Dependabot föreslå uppdateringar.
- **Varför:** Fånga regressionsfel och skapa ett tydligare arbetsflöde vid parallell utveckling. För att garantera att develop deplooy fungerar.
- **Konsekvens/status:** GitHub Actions kör Maven-test samt frontendtester, lint och byggkontroll. Regler för PR/granskning finns. Att testerna körs innebär **inte automatiskt** att misslyckade kontroller blockerar varje merge: den granskade regeluppsättningen saknade uttryckligt angivna obligatoriska statuskontroller. Dependabot-PR:er har ibland blivit liggande.
- **Stöd:** [CI PR #228](https://github.com/chas-challenge-2026/avanza-team3/pull/228), [Dependabot PR #230](https://github.com/chas-challenge-2026/avanza-team3/pull/230), [GitHub-regler](https://github.com/chas-challenge-2026/avanza-team3/rules/24500727).

### Införa rate limiting i API:t
- **När:** 5–7 oktober 2026.
- **Beslut/vägval:** Begränsa upprepade anrop vid inloggning och i API:t med Bucket4j.
- **Varför:** Minska risken för överbelastning och missbruk; frågan lyftes även i CTO-feedbacken. Flera säkerhetspunkter som kan implementeras inom backend diskuterades och detta ansågs vara en lätt att implementera utan att begränsa andras arbete.
- **Konsekvens/status:** Rate limiting är påslaget i normal applikationskonfiguration. Det stängdes endast av i isolerade prestandatester för att mäta backend utan förväntade HTTP 429.
- **Stöd:** [Rate limiting PR #233](https://github.com/chas-challenge-2026/avanza-team3/pull/233), [SecurityConfig](https://github.com/chas-challenge-2026/avanza-team3/commit/ca244c0f).

### Central felhantering och inga interna stacktraces till klienten
- **När:** 7 oktober 2026.
- **Beslut/vägval:** Använda en `GlobalExceptionHandler` och gemensamma felkategorier som `ResourceNotFound`, `ResourceConflict` och `BusinessRuleException`, samt inte exponera stacktraces i API-svar.
- **Varför:** Bättre och säkrare felhantering. Detta baserades till stor del på CTO-feedback.
- **Konsekvens/status:** Standardiserade felresponser i backend; oväntade fel kan loggas internt utan att det interna undantaget skickas till klienten.
- **Stöd:** [Felklasser](https://github.com/chas-challenge-2026/avanza-team3/commit/75827ed5), [Stacktraces](https://github.com/chas-challenge-2026/avanza-team3/commit/fe704aac), [Felhantering PR #245](https://github.com/chas-challenge-2026/avanza-team3/pull/245).

### Komplettera befintlig DTO-validering efter CTO-feedback
- **När:** 8 oktober 2026.
- **Beslut/vägval:** Gå igenom befintlig validering och fylla luckor med exempelvis `@Valid`, `@NotNull`, `@Positive`, formatkontroll och kontroll av tomma PATCH-anrop.
- **Varför:** Förhindra att felaktiga indata når affärslogiken och ge förutsägbar felhantering. Detta gjordes efter feedback från CTO.
- **Konsekvens/status:** Validering och tillhörande tester kompletterades. Arbetet utfördes gemensammt av Kostas och Tilda.
- **Stöd:** [Valideringscommit](https://github.com/chas-challenge-2026/avanza-team3/commit/9bf53195), [PR #247 med detaljer](https://github.com/chas-challenge-2026/avanza-team3/pull/247).

### Begränsa Swagger/OpenAPI-exponering efter ZAP-skanning
- **När:** 19 oktober 2026.
- **Beslut/vägval:** Inaktivera Swagger UI och OpenAPI-dokumentation i deployprofilen, men behålla dem för lokal utveckling.
- **Varför:** OWASP ZAP visade ett High-fynd kopplat till den exponerade Swagger/OpenAPI-ytan.
- **Konsekvens/status:** Den identifierade exponeringen stängdes enligt genomförd kontroll. En ny skanning av bör göras efter omfattande ändringar senaste veckorna.
- **Stöd:** [PR #303](https://github.com/chas-challenge-2026/avanza-team3/pull/303).

### Skilja på enskilda konton och portföljens totala värden
- **När:** 24–28 oktober 2026; Detta diskuterades under möten tidigare både att det behövde ändras, samt hur det behövde ändras. 
- **Beslut/vägval:** Utöka Account till CRUD och lägga till **målallokering per enskilt konto**, separat från den äldre modellen med mål per kontotyp.
- **Varför:** Exempelvis två ISK-konton kan ha olika önskad andel aktier/fonder i backend; användaren behöver kunna se faktiska värden, mål och avvikelser per konto utan att olika sparformer blandas ihop. Detta är baserat på feedback efter demo.
- **Konsekvens/status:** Account CRUD och kontobaserade mål, översikt och avvikelseberäkningar infördes. Den äldre `TargetAllocation`-modellen behölls för befintliga flöden.
- **Stöd:** [Account CRUD](https://github.com/chas-challenge-2026/avanza-team3/commit/bec6fac4), [Account-targets](https://github.com/chas-challenge-2026/avanza-team3/commit/e60967e9), [PR #328](https://github.com/chas-challenge-2026/avanza-team3/pull/328). **Motiv** också dokumenterat i projektanteckningarna.

### Låta live-notiser utgå från avvikelser per konto
- **När:** 31 oktober–1 november 2026.
- **Beslut/vägval:** Beräkna live-notiser utifrån de enskilda kontonas målallokering, i stället för att blanda ihop avvikelser mellan konton.
- **Varför:** Notiser ska avspegla det konto vars strategi avviker. Detta gjordes av Tilda och är baserat på feedback efter demo.
- **Konsekvens/status:** Backend returnerar kontospecifika live-notiser. Dessa är inte samma sak som sparade, avfärdningsbara alerts.
- **Stöd:** [AlertService](https://github.com/chas-challenge-2026/avanza-team3/commit/9ed508d4), [Alert-DTO](https://github.com/chas-challenge-2026/avanza-team3/commit/2dfad53c).

### Göra rebalansering som en kontobaserad simulering med preview och confirm
- **När:** 24 oktober–2 november 2026.
- **Beslut/vägval:** Separera beräkning av handelsförslag från bekräftelse, låta användaren välja tillåtna instrument och skydda bekräftelsen med signerad token och versions-/snapshotkontroller.
- **Varför:** Kunna demonstrera förändring av målallokering utan faktisk handel och minska risken för upprepade eller föråldrade bekräftelser. Räntebärande instrument och råvaruhandel har efterfrågats men ej implementerats i detta än. Detta gjordes baserat på feedback efter demo.
- **Konsekvens/status:** `preview` ska vara läsande; `confirm` uppdaterar enbart projektets databas som en **simulering**, inte något riktigt börskonto. Beräkningslogik, flöde och token/version hanteras separat. Vidare stöd för avgifter, skatt och vissa instrumenttyper diskuterades i backend men har ej implementerats pga tidsbrist.
- **Stöd:** [Rebalanseringsimplementering](https://github.com/chas-challenge-2026/avanza-team3/commit/944c4cd4), [Fullständig föreslagen innehavslista](https://github.com/chas-challenge-2026/avanza-team3/commit/c62942ac), [PR #345](https://github.com/chas-challenge-2026/avanza-team3/pull/345).

### Optimera sammanställningen efter uppmätt prestandaproblem
- **När:** 2-4 november 2026.
- **Beslut/vägval:** Förbättra portföljendpointen genom att aggregera innehav i PostgreSQL innan Java utför värderings- och allokeringsberäkningar. Behålla befintliga API-svar och pagination.
- **Varför:** Med 10 000 testinnehav utfördes för mycket arbete per dashboardanrop till holding; ursprungligt k6-test överskred P95-kravet på 200 ms.
- **Konsekvens/status:** Lokalt test med 100 virtuella användare i 60 sekunder gav P95 cirka **669,71 → 33,25 ms** för `/api/portfolio`; 200 Maven-tester gick igenom och 5 hoppades över(enligt egna inställningar). Ett försök att öka Hikari-poolen 10 → 30 gav sämre resultat och återställdes. Testet använde ett konto och rate limiting avstängt.
- **Stöd:** Lokala k6-sammanfattningar, backendtestresultat och projektets prestandadokumentation. När PR gjorts kommer detta kompleteras med länk.

## Medvetna avgränsningar och olösta vägval

### UUID-migrering sköts upp
- **När:** Efter CTO-feedback.
- **Vägval:** Genomföra andra säkerhets- och leveransåtgärder före ett genomgripande byte från sekventiella ID:n till UUID.
- **Motivering:** Bytet påverkar databasrelationer, API-kontrakt, tester och potentiellt frontend. En säker migration kräver samordnad ändring över flera delar.
- **Status:** **Inte implementerat.** Detta var ett individuellt prioriteringsbeslut som bör redovisas öppet, inte beskrivas som ett färdigt teamgemensamt säkerhetsskydd. UUID ersätter aldrig ägarkontroll.
- **Stöd:** Anteckningar från CTO-feedforward och efterföljande projektprioritering; ingen UUID-migrering har gjorts.

### Simulerad marknadsdata och riskhistorik är inte produktionsdata
- **Vägval/nuvarande begränsning:** Låta demonstrations-/fallbackprovidern och simulerad historik finnas kvar under projektets tidsram.
- **Konsekvens:** Okända tickers kan få ett generiskt pris i stället för ett verifierat marknadspris, och vissa riskvärden bygger på simulerad historik. Detta är **en brist som kan ge missvisande siffror**, inte en säker metod att hantera prisbortfall.
- **Status:** Dokumenterat som känd brist.
- **Stöd:** `FallbackMarketDataProvider`, `PortfolioRiskService` och backendens v2-dokumentation.

### Fullständig driftövervakning och sårbarhetskontroll ingår inte i verifierad slutstatus
- **Vägval/nuvarande begränsning:** Test, CI och en ZAP-skanning prioriterades, medan fullständig audit trail, manipulationsskyddade loggar, aktiv monitorering och obligatorisk automatiserad säkerhetsskanning i CI inte har verifierats som genomförda.
- **Konsekvens:** Produkten kan inte beskrivas som produktionssäker bara för att de lokala testerna är gröna. Dependabot finns, men dess PR:er behöver aktivt granskas.
- **Stöd:** CTO-anteckningar, GitHub Actions- och Dependabot-konfiguration samt projektets dokumenterade restpunkter.

### Förslag som diskuterades men inte ska räknas som genomförda beslut
- Avancerad skatteberäkning för ISK/depå, kvittning av vinster och förluster i Depå, exakta handelsavgifter och mer avancerad rebalansering.
- En fullständig historisk backtestinglösning mot verkliga femårsdata för upp till 500 instrument.
- Fullständig återhämtningsstrategi med cachade verifierade priser, exponential backoff och aktiv driftövervakning.

### Kontakt och gemensamma beslut inom backend
Kostas och Tilda har haft regelbunden kontakt via Discord och Slack, och med det tagit gemensamma beslut. Samt hjälpts åt med problem och diskuterat lösningar tillsammans vid upprepade tillfällen.
