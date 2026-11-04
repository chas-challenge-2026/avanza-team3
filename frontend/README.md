# Avanza Portal – Team 3

Avanza Portal är en portföljapplikation byggd med React, Spring Boot och PostgreSQL. Skolprojekt på Chas Academy med fiktiva uppgifter. Backend får hjälp av C- och C++-moduler för valutaomvandling och riskmått.

## Vad appen gör

- **Överblick:** totalt portföljvärde, konton, fördelning per kontotyp, portföljhälsa och live-varningar om avvikande allokering
- **Portföljhälsa:** riskpoäng, diversifiering, innehav, valutaexponering och diagram. Mål för allokering kan visas och ändras per konto
- **Rebalansering:** välj ett konto och vilka instrument som får användas, och få ett förslag på ny fördelning
- **Notiser:** aktuella avvikelser mot målallokeringen, med länk vidare till rebalansering

## Delarna och deras README

| Del             | Teknik                                                        | README                                                 |
| --------------- | ------------------------------------------------------------- | ------------------------------------------------------ |
| Frontend        | React, TypeScript, Vite, Material UI                          | [READMEFRONTEND.md](READMEFRONTEND.md)                 |
| Backend         | Java 21, Spring Boot, PostgreSQL                              | [backend/Backend-README.md](backend/Backend-README.md) |
| Native-moduler  | C och C++ (valutakurser och riskmått), laddas av Java via JNA | [native/README.md](native/README.md) (på engelska)     |
| Start och drift | Docker                                                        | [docs/v2/README2.md](docs/v2/README2.md)               |

## Snabbstart

Förutsättning: Docker Desktop. Kommandona nedan är för Windows PowerShell.

1. Gå till `infra` och sätt en JWT-secret (minst 32 tecken):
   ```
   cd infra
   $env:JWT_SECRET="test-secret-key-with-at-least-32-characters"
   ```
2. Starta allt:
   ```
   docker compose up --build
   ```
3. Öppna `http://localhost:8082`

Testanvändare, stopp och omstart, backendtester och felsökning finns i [docs/v2/README2.md](docs/v2/README2.md).

### Frontend-utveckling

Låt backend och PostgreSQL köra i Docker och starta sedan frontend från projektets rotmapp:

```
cd frontend
npm ci
npm run dev
```

Frontend körs normalt på `http://localhost:5173` och skickar API-anrop till backend på port 8082. Fler kommandon (`npm run build`, `npm run lint`, `npm run test:run`) finns i [READMEFRONTEND.md](READMEFRONTEND.md).

## Mer dokumentation

- [Arkitektur v2](docs/v2/architecture.md)
- [Mål och resultat för v2](docs/v2/v2-targets.md)
- [Drift och felsökning](docs/v2/DRIFT.md)
- [Beslutslogg](backend/AvanzaPortal/beslutslogg.md)

## Teamet

- Frontend: Isabelle Franzén, Max Mattiasson, Sascha Bergenås
- Java: Konstantinos Nascos, Tilda Nivåsen, Felix Lidén
- C/C++: Jonathan Käcker, Alexander Safapay
