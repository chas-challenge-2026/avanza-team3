# Arkitektur — Avanza Portföljhälsa v2

## Övergripande design
Webb/React + Typescript
->
JSON/REST API
->
Spring Boot 3.2.11 med Java 21, på port 8082
Spring Security: JWT-filter, metodsäkerhet, rate limiting
RestController: auth, accounts, holdings, instruments, targets, allocation, alerts, rebalance, portfolio, risk, market
Srvice: behörighetskontroller, affärsregler, beräkningar
Spring Data: JPA/Repository
->
PostgreSQL 15 med flyway v1-v9
->
MarketDataService
->
Native FX via JNA/C++
Fallbackpriser och fallbackvalutor(simulerade)
->
RiskService
->
native C-modul via JNA

Dockerfile bygger allt.

## Teknikstack sammanfattning

| Lager | Teknik                |
|---|-----------------------|
| Ramverk | Spring Boot 3.2.11    |
| Vy-motor | React 19 + Typescript |
| Datbaslager | JPA                   |
| Databas | PostgreSQL 15         |
| Auth | JWT                   |
| Bygg | Maven 4.0.0, Java 21    |
| Deploy | Docker Compose        |

## Vad som saknades (avsiktligt) och vad som implementerades

- **Inget servicelager** — Tydlig uppdelning med service, repo, entitet, dto, controller samt exceptionhandling
- **Ingen repository-abstraktion** — SQL-strängar hanteras korrekt via JpaRepositories
- **Ingen säkerhetsfilter** — implementerat JWT-autentisering
- **Ingen pagination** — pagination på listendpoints
- **Ingen FX-integration** — FX integration klar
- **Inga tester** — unit samt integrationstester med bred täcknning utöver ställda krav

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
