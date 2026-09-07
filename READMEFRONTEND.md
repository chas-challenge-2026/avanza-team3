# Avanza Portal - Frontend

Frontend-delen av Avanza Portal byggs om med React och TypeScript, med fokus på en modern, komponentbaserad och skalbar arkitektur.

## Planen

Målet är att skapa en uppdaterad frontend som hämtar och presenterar data från backend på ett tydligt och användarvänligt sätt.

Vi har valt att behålla den ursprungliga navigeringen i projektet, men kompletterar den med en ny sektion för risköversikt. Denna sektion har vi valt att benämna **Portföljhälsa**, eftersom begreppet upplevs som mer tryggt och lättillgängligt för användaren än exempelvis ”Riskhantering” eller ”Risköversikt”.

## Teknik

Frontend kommer att använda sig av:

- React 18
- TypeScript
- Vite
- React Router
- TanStack Query för API-anrop och server-state
- Material UI för UI-komponenter
- MUI X Charts för grafer och visualiseringar
- Font Awesome för ikoner
- ESLint för kodkvalitet

## Nuvarande status

Frontend-projektet innehåller redan:

- React- och TypeScript-konfiguration
- Vite som utvecklingsserver
- Grundläggande routing
- Inloggningssida
- Överblick
- Återanvandbara komponenter
- Material UI
- MUI X Charts
- Mockdata för frontendutveckling

Fölande behöver läggas till eller färdigställas:

- TanStack Query
- Riktiga API-anrop mot backend
- Integration mellan autentisering och backend
- Ersättning av mockdata
- Färdigställande av grafer
- Dokumentation av frontendens arkitektur

### Användning av AI och designverktyg i designprocessen

#### Designgrund

I projektet har vi använt AI som ett stöd genom både design- och utvecklingsprocessen.

Som utgångspunkt för vår ursprungliga design tog vi del av kundens nuvarande design och de designelement som presenterades i videomaterialet. Har vi valt att behålla delar av den befintliga visuella layouten, samtidigt som vi uppdaterar och moderniserade designen för att skapa ett mer användarvänligt gränssnitt.

Den första designen och strukturen för applikationen togs fram av projektgruppen i Figma.

#### AI som kompletterande verktyg

När den ursprungliga designen var framtagen använde vi AI för att vidareutveckla och förfina designen. AI användes bland annat för att få förslag på förbättringar kring:

- Layout och struktur
- Färgval och visuell hierarki
- Användarupplevelse

Förslagen fungerade som underlag och inspiration i det fortsatta designarbetet. Projektgruppen bedömde själva vilka förändringar som var relevanta för projektet.

#### CSS och implementation

Även vid framtagningen av CSS använde vi AI som stöd. Vi fick förslag på exempelvis struktur, responsivitet, spacing och styling.

Förslagen granskades och anpassades sedan efter projektets behov och den design vi hade tagit fram i Figma.

#### Projektgruppens ansvar

AI har inte använts för att skapa projektet eller designen från grunden, utan som ett kompletterande verktyg genom processen. Den ursprungliga designen baserades på kundens befintliga design och anpassades av projektgruppen med målet att modernisera och förbättra användarupplevelsen.

De övergripande designbesluten, anpassningarna och den slutliga implementeringen har gjorts av projektgruppen.

## Installation

### Förhandskrav

- Node.js 18 eller senare
- npm

### Installera projektet

Navigera till frontend-mappen:

```bash
cd frontend
```

Installera projektets beroenden:

```bash
npm install
```

## Köra projektet lokalt

Starta utvecklingsservern:

```bash
npm run dev
```

Frontend kör på:

```text
http://localhost:5173
```
