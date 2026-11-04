# Avanza Portal

Avanza Portal är en portföljapplikation byggd med React, Spring Boot och PostgreSQL.

## Förutsättningar

För att starta hela applikationen:

- Docker Desktop

För lokal utveckling och testning:

- Node.js och npm – frontend
- Java 21 och Maven – backend

## Starta applikationen

Alla kommandon är anpassade för Windows PowerShell.

### 1. Konfigurera JWT-secret

Gå till `infra` från projektets rotmapp:

```powershell
cd infra
```
sätt en JWT-secret.
```powershell
$env:JWT_SECRET="test-secret-key-with-at-least-32-characters"
```

Nyckeln ovan fungerar för lokal testning men ska inte användas i produktion. Använd en egen slumpgenererad nyckel för andra miljöer.

Miljövariabeln gäller endast i den aktuella PowerShell-sessionen och behöver sättas igen om en ny terminal används.

### 2. Starta med Docker

```powershell
docker compose up --build -d
```

Kontrollera att tjänsterna körs:

```powershell
docker compose ps
```

Öppna applikationen på:

http://localhost:8082

**Testanvändare:**

| E-post | Lösenord |
|---|---|
| anna@example.com | password123 |
| erik@example.com | password123 |

## Frontendutveckling

Backend och PostgreSQL behöver vara igång via Docker.

Öppna en ny terminal från projektets rotmapp:

```powershell
cd frontend
npm ci
npm run dev
```

Frontend körs normalt på http://localhost:5173.

API-anrop vidarebefordras till backend på port 8082.

### Frontendtester

Kör från `frontend`:

```powershell
npm run test:run
npm run lint
npm run build
```

## Backendtester

Öppna en ny terminal från projektets rotmapp:

```powershell
cd backend/AvanzaPortal
mvn test
```

PostgreSQL-integrationstestet för rebalansering har genomförts och godkänts. För att köra det på nytt krävs en separat testdatabas och miljövariabeln REBALANCE_PG_TEST=true.

Se backendens README för instruktioner.

## Stoppa applikationen

Från `infra`:

```powershell
docker compose down
```

För att starta igen:

```powershell
docker compose up -d
```

Efter kodändringar som kräver ombyggnad:

```powershell
docker compose up --build -d
```

## Felsökning och driftsättning

Se [DRIFT.md](DRIFT.md) för mer information om Docker, databasåterställning, vanliga fel och driftsättning.


## Dokumentation

Mer detaljerad dokumentation finns i följande filer:

- [Backend – API, tester, säkerhet och feedforward](backend/Backend-README.md)
- [Frontend](READMEFRONTEND.md)
- [C/C++ och native-moduler](native/README.md)
- [Arkitektur v2](docs/v2/architecture.md)
- [Mål och resultat för v2](docs/v2/v2-targets.md)
- [Beslutslogg](backend/AvanzaPortal/beslutslogg.md)

Backenddokumentationen innehåller även resultat från k6-belastningstester,
OWASP ZAP-skanning, genomförda tester och kända begränsningar.
