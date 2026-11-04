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
