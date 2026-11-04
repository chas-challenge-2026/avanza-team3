### Lasttest med k6 – 2026-11-03

Ett lasttest gjordes mot en lokal Docker-miljö med PostgreSQL 15 och 10 000 syntetiska innehav.

- Belastning: 100 virtuella användare under 60 sekunder.
- Autentisering: JWT, samma testkonto för samtliga virtuella användare.
- Rate limiting: tillfälligt inaktiverades i lasttestmiljön efter ett första försök med mycket sämre resultat.
- `GET /api/holdings`: P95 **29,23 ms** – godkänt.
- `GET /api/portfolio`: P95 **cirka 1 300 ms** – ej godkänt.
- HTTP-fel: **0 %**.
- Genomförda kontroller: **8 278**, samtliga lyckades.

**Slutsats:** Systemet hanterade testbelastningen utan HTTP-fel, men uppgiftens P95-krav på under 200 ms uppfylldes inte för portföljsammanställningen.

En möjlig flaskhals är att dashboardens beräkningar fortfarande behöver bearbeta samtliga innehav trots att innehavslistan är paginerad. -kollat, inte hela problemet.

**Begränsning:** Testet använde ett gemensamt användarkonto och verifierar därmed inte prestandan för 100 separata användarkonton.

Resultatfil: `infra/k6-summary-100vu.json`.


Prestandamätningar genomfördes enligt projektkraven med k6 och 10 000 testinnehav.

Jag upptäckte att dashboarden hämtade och värderade samtliga 10 000 innehav individuellt vid varje anrop, även om pagination bara visade exempelvis 20 eller 100 innehav. Vid 100 samtidiga virtuella användare skapade detta hög belastning och svarstiderna överskred kravet på P95 under 200 ms.

Lösningen blev att optimera backend så att innehaven först grupperas i databasen och därefter värderas i Java. Efter ändringen förbättrades P95 för portfolio från cirka 670 ms till 33 ms. Alla 200 körda backendtester gick också igenom.

Transparens kring arbetet: k6 var helt nytt för mig. Jag genomförde testerna och verifierade resultaten själv, men använde omfattande AI-stöd genom processen, både för felsökning, analys och framtagning av kodändringarna.

Lasttestet utfördes i en isolerad testmiljö med ett gemensamt testkonto för 100 virtuella användare och rate limiting avstängt.

