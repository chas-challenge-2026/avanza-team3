# Avanza Portföljhälsa – frontend

Det här dokumentet beskriver endast frontend-applikationen i `frontend/`. Den
är gränssnittet för att följa en investeringsportfölj, dess risk och
fördelning. Frontenden hämtar och uppdaterar data via backendens REST-API.

## Från v1 till v2

| v1 | v2 |
| --- | --- |
| Thymeleaf-sidor renderade av backend | React-baserad single-page application (SPA) |
| Bootstrap och vyer kopplade till Spring-applikationen | Fristående frontend med TypeScript och Vite |
| Navigation mellan serverrenderade sidor | Klientbaserad routing med React Router och skyddade vyer |
| Begränsad, sidbaserad presentation | Återanvändbara komponenter, diagram och interaktiva vyer |
| Gränssnitt tätt kopplat till backend | Data hämtas och ändras genom REST-anrop till `/api` |

V2 har separata sidor för överblick, portföljhälsa, rebalansering och notiser.
Inloggade användare får ett gemensamt gränssnitt med sidomeny, sidhuvud och
sidfot.

## Funktioner

- **Inloggning:** formulär för inloggning och skyddade sidor.
- **Överblick:** totalt portföljvärde, konton, fördelning per kontotyp,
  portföljhälsa och live-varningar om avvikande allokering.
- **Portföljhälsa:** riskpoäng, diversifiering, innehav, valutaexponering och
  diagram. Mål för allokering kan visas och ändras per konto.
- **Rebalansering:** välj konto och hantera en lista över innehav som godkänts
  för rebalansering.
- **Notiser:** visa live-varningar om allokering och hämta sparade notiser.

## UX

Gränssnittet är på svenska och använder en gemensam navigering för att göra
portföljens olika delar lätta att hitta. Överblicken prioriterar portföljvärde
och fördelning; diagram, tabeller och riskindikatorer kompletterar siffrorna.
Förklarande texter hjälper användaren att förstå bland annat kontotyper,
portföljhälsa och risk.

### Design och komponentval

Material UI-komponenter har använts som grund för delar av gränssnittet. På så
sätt har vi kunnat återanvända färdiga komponenter i stället för att bygga varje
grundkomponent från grunden. Det har sparat utvecklingstid och gett oss mer
utrymme att fokusera på funktionalitet och applikationens logik.

AI har använts som stöd i designarbetet för att utforska idéer och ta fram
förslag på hur gränssnittet kan få ett modernare uttryck. Förslagen har fungerat
som inspiration och har utvärderats och anpassats av projektgruppen efter
projektets behov och den önskade användarupplevelsen.

## Teknikstack

- React 19 och TypeScript
- Vite 8 för utveckling och produktionsbygge
- React Router 7 för navigering
- Material UI 9 och MUI X Charts 9
- Emotion, CSS Modules och Font Awesome
- REST-anrop med Fetch API; autentisering och användartillstånd hanteras med
  React Context och egna hooks
- ESLint, Vitest och React Testing Library för kodkvalitet och tester

Data hämtas från backendens `/api`-endpoints. Vites utvecklingsserver proxar
`/api` till `http://localhost:8082`. Frontenden använder inte TanStack Query.

## Kända begränsningar

- Rebalanseringssidan använder för närvarande mockdata och lokalt React-tillstånd.
  Listan sparas inte som en rebalansering i backend och inga affärer genomförs.
- Avfärdning av notiser uppdaterar gränssnittets lokala tillstånd; den sparade
  notisen markeras inte som avfärdad via backend. Avfärdade live-varningar
  återställs när sidan laddas om.
- Vissa värden i portföljhälsan, exempelvis den visade förändringen på 5 %, är
  platshållare och inte en beräkning från historisk portföljdata.
- Utloggningen nollställer användaren i gränssnittet, men den nuvarande
  logout-funktionen tar inte bort token från `localStorage`.
- Vissa interaktiva ikoner är inte implementerade som semantiska knappar, vilket
  kan begränsa tangentbords- och skärmläsarstöd.
- Frontenden är beroende av backendens API och testdata. Vites proxy kräver att
  backend körs lokalt på port 8082 för API-funktionerna.

## Köra lokalt

Förutsättningar: Node.js och npm. API-funktionerna kräver även att backend körs
på `http://localhost:8082`.

```bash
cd frontend
npm install
npm run dev
```

Vite skriver ut adressen till utvecklingsservern i terminalen.

Övriga användbara kommandon:

```bash
npm run build       # TypeScript-kontroll och produktionsbygge
npm run lint        # ESLint
npm run test:run    # Kör testerna en gång
```
