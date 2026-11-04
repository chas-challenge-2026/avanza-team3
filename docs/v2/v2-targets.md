# v2 – mål, levererad funktionalitet och verifiering

Den ursprungliga specifikationen finns i [docs/v2-targets.md](../v2-targets.md). Denna fil beskriver **utfall i v2** per 4 november 2026. Status betyder inte att all produktionstestning är klar.

## Teknisk status

| Ursprungligt mål | Status i v2 | Bevis/begränsning                                                                                            |
| --- | --- |--------------------------------------------------------------------------------------------------------------|
| Java 21 och Spring Boot 3.2 | Implementerat | `pom.xml` anger Java 21 och Spring Boot 3.2.11                                                               |
| Controller → service → repository | Implementerat för huvudflöden | Separata paket, DTO:er och affärsregler                                                                      |
| JPA/Hibernate, Flyway, PostgreSQL 15 | Implementerat | Flyway V1–V9, Docker PostgreSQL 15; fler uppgraderingsscenarier kan testas                                   |
| Spring Security, BCrypt, JWT | Implementerat | Inloggning och användarisolering; logout spärrar inte JWT                                                    |
| IDOR-skydd/ägarkontroller | Implementerat | Ägarskapskontroller och relevanta tester finns i backend                                                                  |
| Pagination för listor | Implementerat för centrala endpoints | `GET /api/holdings/summary` är medvetet opaginerad                                                           |
| React + TypeScript | Implementerat | **React 19** i leveransen, inte React 18 i ursprungliga kravtexten                                           |
| Native riskmodul med JNA | Implementerat | C-beräkningar finns; riskhistorik är delvis mockad                                                           |
| FX-pipeline/native-modul | Implementerat | JNA-brygga finns; fallbackpriser/kurser och historisk data är begränsande                                    |
| Backtesting enligt C/C++-gruppens delmål | Hanteras av C/C++-gruppen | Se deras native-README för beskrivning och verifiering; status bedöms inte i backenddokumentation            |
| >80 % kodtäckning i servicelagret | Servicemetodernas testtäckning är över 80% |  Enhets- och integrationstester har genomförts. Någon exakt procentuell kodtäckningsmätning redovisas inte här. |
| OWASP ZAP: inga High-severity fynd | **Ej verifierat för slutversionen** | Tidigare High-fynd relaterat till Swagger åtgärdades i deploy; ny full skanning återstår men begränsas av nära deadline |
| k6 P95 < 200 ms vid 100 virtuella användare och 10 000 innehav | **Uppfyllt i beskriven lokal testmiljö** | `/api/portfolio` P95 33,25 ms efter optimering; 100 samtidiga VU fungerade men alla använde samma konto/JWT  |
| Flyway och PostgreSQL 15 | Implementerat och testat | Flyway V1–V9, PostgreSQL 15 i Docker och godkänt PostgreSQL-integrationstest för rebalansering                        |

## Lasttest 3 november 2026 – historik och slutresultat

Det första experimentet med 100 virtuella användare i 60 sekunder och 10 000 syntetiska innehav gav `/api/portfolio` P95 **cirka 1 300 ms** och `/api/holdings` P95 **29,23 ms** (se `infra/k6-summary-100vu.json`). Det var **inte** ett godkänt resultat.

Vid efterföljande **jämförbara tester med `size=100`** för portföljen, 100 VU och 60 sekunder blev resultatet:

| Mätvärde | Före optimering | Efter optimering |
| --- | ---: | ---: |
| `/api/holdings` P95 | 20,06 ms | **5,64 ms** |
| `/api/portfolio` P95 | 669,71 ms | **33,25 ms** |
| HTTP-fel | 0 % | **0 %** |
| Iterationer | 4 628 | **5 748** |

**Slutsats:** Projektkravet på P95 under 200 ms för de två uppmätta endpoints uppnåddes **i den isolerade testmiljön** efter databasaggregering av innehav före Java-värdering. Förbättringen av portfölj-P95 var ungefär **95 %** mellan jämförbara körningar. Resultatfiler: `infra/k6-summary-size100-100vu.json` och `infra/k6-summary-optimized-100vu.json`.

**Testbegränsningar:** Ett gemensamt konto/JWT för alla 100 VU, nästan samma instrument i alla 10 000 innehav, rate limiting avstängt enbart under mätningen, lokala resurser i Docker. Resultaten bevisar **inte** samma beteende i drift eller för 100 olika användarkonton. Vi testade även HikariCP-pool 10 → 30 utan förbättring och återställde till 10.

**Backendtest:** Standardkörningen den 3 november gav 205 tester: **200 godkända, 0 fel och 5 överhoppade**. Backendens tester har enligt teamet körts, inklusive separat godkänt PostgreSQL-integrationstest för rebalansering. **Applikationen är också driftsatt på webben och har funktionstestats.** [CI på develop, 4 november, success](https://github.com/chas-challenge-2026/avanza-team3/actions/runs/41818759995).

## Funktioner utöver den ursprungliga enkla MVP:n

- CRUD för konton, instrumentkatalog, fler instrumentkategorier och filtrerade innehav.
- Målallokering per **enskilt konto**, utöver äldre mål per kontotyp.
- Kontobaserade live-notiser vid avvikelse över fem procentenheter.
- Simulerad rebalansering.
- Optimerad aggregering för portföljöversikt vid last.

## Inte färdigverifierat / vidare arbete

Produktionstrogna marknadspriser, verklig riskhistorik i backendflödet, ny heltäckande ZAP-skanning, fullständig audit trail, driftmonitorering och UUID-migration kvarstår som förbättringar. C/C++-gruppens egna backtestingfunktioner och tester beskrivs i deras dokumentation. k6 har genomförts med 100 samtidiga virtuella användare och godkända svarstider, men med ett gemensamt användarkonto/JWT; test med 100 olika konton är en annan testuppställning. Täckningen av servicemetoder har däremot granskats och bedömts tillräcklig av teamet. Beroendeuppdateringar från Dependabot kräver aktiv granskning.
