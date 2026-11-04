# Arkitektur — Avanza Portföljhälsa v2

## Övergripande design

Webbläsare
|
v
React 19 + TypeScript
|
v
JSON/REST API
|
v
Spring Boot 3.2.11 / Java 21 (port 8082)
|
+-- Spring Security
|      JWT, metodsäkerhet, rate limiting
|
+-- REST Controllers
|      Auth, Account, Holding, Instrument,
|      Allocation, Alert, Rebalance, Portfolio,
|      Market, Risk
|
+-- Services
|      Behörighetskontroller, affärsregler,
|      transaktioner och beräkningar
|
+-- Spring Data JPA / Repositories
|      |
|      v
|   PostgreSQL 15
|   Flyway-migrationer V1–V9
|
+-- MarketDataService
|      Priser och valutakurser
|      Native FX via JNA/C++
|      Simulerad fallbackdata
|
+-- RiskService
Riskberäkningar via C/JNA
Delvis simulerad historik

Dockerfile bygger frontend, backend och native-moduler.

## Teknikstack sammanfattning

| Lager       | Teknik                                 |
|-------------|----------------------------------------|
| Ramverk     | Spring Boot 3.2.11                     |
| Frontend    | React 19 + Typescript                  |
| Datbaslager | JPA                                    |
| Databas     | PostgreSQL 15                          |
| Auth        | JWT                                    |
| Bygg        | Maven 3.9, Java 21, npm/vite och Cmake |
| Deploy      | Docker Compose                         |

## Brister i v1 och genomförda förbättringar i v2

- **Inget servicelager** — Tydlig uppdelning med service, repo, entitet, dto, controller samt exceptionhandling
- **Ingen repository-abstraktion i v1** — dataåtkomst har flyttats till Spring Data JPA-repositories.
- **Ingen säkerhetsfilter** — implementerat JWT-autentisering
- **Ingen pagination** — pagination på listendpoints
- **Ingen FX-integration i v1** — native FX-integration via JNA har implementerats. Simulerade fallbackkurser används fortfarande i vissa fall.
- **Inga tester i v1** — enhets- och integrationstester har implementerats och genomförts. Servicemetodernas testtäckning har granskats och bedömts som tillräcklig.

## Ansvar per backenddel förklarat

| Paket/del | Uppgift |
| --- | --- |
| `auth`, `security`, `config` | Login, JWT, Spring Security, metodsäkerhet, rate limiting |
| `account` | Konton, CRUD, ägarskapskontroll, saldo och rebalanseringsversion |
| `holding`, `instrument` | Innehav och instrumentkatalog |
| `targetallocation` | Äldre mål per **kontotyp**, fortfarande ett separat API |
| `accountallocation` | Nya mål per **enskilt konto** och faktisk allokering |
| `alert` | Sparade notiser och dynamiska live-notiser per konto |
| `rebalance` | Preview, beräkning, signerade förslag och simulerad confirm |
| `portfolio`, `dashboard` | Portföljvärdering och aggregerad översikt |
| `market` | Priser och växelkurser via providers |
| `risk`, `nativebridge` | Riskmått från C via JNA |


## Rebalansering (simulerad handel)

```text
POST /api/accounts/{id}/rebalance/preview
  -> verifiera kontoägare, instrument och konto-mål
  -> läs innehav, saldo, priser och valda instrument
  -> beräkna before/after, trades, proposedHoldings
  -> returnera kortlivad signerad proposalToken (ingen skrivning)

POST /api/accounts/{id}/rebalance/confirm
  -> verifiera token och användare/konto
  -> lås konto och kontrollera rebalance_version + snapshot
  -> räkna om förslaget
  -> uppdatera innehav + cash_balance_sek i en transaktion
  -> öka rebalance_version
```
