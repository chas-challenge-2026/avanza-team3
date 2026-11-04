# Avanza Portföljhälsa – frontend

Det här dokumentet beskriver endast frontend-applikationen i `frontend/`. Den
är gränssnittet för att följa en investeringsportfölj, dess risk och
fördelning. Frontenden hämtar och uppdaterar data via backendens REST-API.

## Hoppa till

- [Från v1 till v2](#från-v1-till-v2)
- [Funktioner](#funktioner)
- [UX](#ux)
- [Feedforward](#feedforward)
- [Design och komponentval](#design-och-komponentval)
- [Teknikstack](#teknikstack)
- [Kända begränsningar](#kända-begränsningar)
- [Köra lokalt](#köra-lokalt)
- [Beslutslogg](#beslutslogg)

## Från v1 till v2

| v1                                                    | v2                                                       |
| ----------------------------------------------------- | -------------------------------------------------------- |
| Thymeleaf-sidor renderade av backend                  | React-baserad single-page application (SPA)              |
| Bootstrap och vyer kopplade till Spring-applikationen | Fristående frontend med TypeScript och Vite              |
| Navigation mellan serverrenderade sidor               | Klientbaserad routing med React Router och skyddade vyer |
| Begränsad, sidbaserad presentation                    | Återanvändbara komponenter, diagram och interaktiva vyer |
| Gränssnitt tätt kopplat till backend                  | Data hämtas och ändras genom REST-anrop till `/api`      |

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

## Feedforward

Vi har tagit till oss av feedforwarden genom att utgå från Annas perspektiv:
förstår hon vad hon ser, och vet hon vad hon ska göra härnäst? Tydlighet var en
röd tråd i återkopplingen och har därför varit ett huvudfokus i arbetet.

Vi har lagt till förklaringar av begrepp som riskpoäng, portföljhälsa och
målallokering. På mobil visas förklaringarna i popovers för att gränssnittet ska
vara snabbt att överblicka, medan de visas som text på desktop där det finns
större utrymme för analys. Vi har också gjort siffror mer självförklarande,
exempelvis genom att visa hur innehaven fördelar sig under ”Antal innehav”.

Notiserna visar mål, nuläge och avvikelse var för sig. De leder vidare till
rebalansering med rätt konto förvalt, så att Anna kan agera direkt när något
avviker. Målallokeringen går att ändra med både reglage och inmatningsfält.

Återkopplingen om att användaren inte behöver lägga till innehav ledde till en
större förändring: vi tog bort den separata Innehav-sidan och byggde i stället
Rebalansering. Där väljer Anna ett konto och vilka instrument som får användas,
och får ett förslag på hur kontot kan rebalanseras. Cirkeldiagram före och efter
visas bredvid varandra, så att effekten går att se innan förslaget bekräftas.
På så sätt kan Anna gå från att upptäcka en avvikelse till att agera på den,
utan att själv behöva lägga in innehav.

Vi har inte hunnit med allt. Exempelvis har historik och backtesting fått stå
tillbaka för funktioner som bedömdes ge större nytta för användaren.

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

- Rebalanseringsförslaget hämtas från backendens API, men listan med godkända
  instrument hanteras tillfälligt i frontendens tillstånd och försvinner när
  sidan laddas om.
- Bekräftelse av rebalansering skickas till backendens API. Frontenden är däremot
  inte kopplad till en extern mäklare för att lägga riktiga köp- eller säljorder;
  priser och förslag kan vara simulerade.
- Notiser visar aktuella avvikelser från målallokeringar, inte en historik över
  sparade eller tidigare avfärdade notiser. En avvikelse visas när den överstiger
  5 procentenheter, och endast den största avvikelsen per konto visas.
- Förändringen på 5 % som visas i portföljhälsan är ett fast värde och inte
  beräknad från historisk portföljdata.
- Utloggningen nollställer användaren i gränssnittet, men tar för närvarande inte
  bort autentiseringstoken från `localStorage`.
- API-baserade funktioner kräver att backendens API är tillgängligt.
  Vites utvecklingsproxy förväntar sig att backend körs lokalt på port 8082.

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

## Beslutslogg

1. **Rebalansering som huvudflöde** — Appen har sidorna Överblick,
   Portföljhälsa, Rebalansering och Notiser. Innehav-sidan ersattes av
   Rebalansering, så innehaven hanteras i samma flöde som förslagen på köp och
   sälj. Fokus på automatisk omfördelning förenklar investeringar för användare
   som inte vill fatta varje affärsbeslut manuellt.

2. **Målallokering per konto** — Användaren kan sätta individuella mål för ISK,
   KF och depå. Det gör det möjligt att anpassa kontona efter olika
   investeringsstrategier.

3. **Tre kategorier för målallokering** — Vi använder aktier, räntor och övrigt.
   Fonder är inte en egen kategori eftersom de kan innehålla flera tillgångsslag;
   det förenklar allokeringsmodellen och rebalanseringslogiken.

4. **Rebalansering av ett konto i taget** — Två cirkeldiagram visar fördelningen
   före och efter, så användaren kan förstå förslaget och se dess effekt direkt.

5. **Godkända instrument gäller hela portföljen** — Användaren väljer instrument
   en gång och behåller kontroll över vad som får användas i rebalanseringen.
   Urvalet började som mockdata, vilket gjorde det möjligt att färdigställa
   frontendflödet innan backendstödet var klart.

6. **Portföljhälsa utgår från användarens mål** — I stället för att enbart visa
   generella riskmått och innehavsspridning kopplas hälsan till måluppfyllelse,
   vilket gör resultatet mer personligt och lättare att förstå.

7. **Tillgångsslag hämtas från instrumentdata** — Summary-data kopplas till
   instrumentens riktiga typ eftersom summary klassificerar obligationer som
   "Övrigt". Det ger en mer korrekt fördelning.

8. **Responsiva förklaringar** — Information visas som popover på mobil för att
   spara utrymme och som text på desktop där det finns mer plats.

9. **Sammanhållen visuell stil** — Ett gemensamt MUI-tema med projektets gröna
   primärfärg och en gemensam, dämpad färgpalett håller komponenter och
   statusfärger konsekventa och lättlästa.

10. **Demodata via Postman-collection** — Eftersom backendens seed-data bara
    skapas när en ny databas initieras gör collectionen det enklare för alla att
    använda samma testdata.

11. **Notiser leder vidare till rätt åtgärd** — En notis visar den största
    avvikelsen per konto när den överstiger 5 procentenheter och länkar till
    rebalansering med kontot förvalt. Det gör varningen tydlig och sparar ett
    steg.

## Delaktiga – Frontend JavaScript

- [Isabelle Franzén](https://github.com/isabellea-f)
- [Max Mattiasson](https://github.com/maxmattiasson)
- [Sascha Bergenås](https://github.com/Sascha-Bergenas)
