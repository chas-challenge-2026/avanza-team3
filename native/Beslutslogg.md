Beslutslogg

Beslut: Vi valde att inte färdigställa och integrera en backtestmodul i projektets leverans.

Bakgrund: Under arbetet identifierade vi osäkerheter kring modulens förväntade omfattning, vilka investeringsstrategier den skulle stödja och vilka antaganden simuleringen skulle bygga på. Vi behövde också fördjupa våra kunskaper om backtesting för att kunna bedöma resultatens tillförlitlighet.

Vår research visade att den tänkta lösningen ställde krav på historisk instrumentdata med tillräcklig täckning och kvalitet. Under projektets tidsram hittade vi ingen kostnadsfri datakälla som vi bedömde uppfyllde alla våra behov av historisk instrumentdata. Vilken prisdata som behövs beror dessutom på strategin och simuleringens detaljnivå.


Övervägande: En förenklad prototyp hade kunnat vara möjlig, men hade krävt tydliga förenklingar och ytterligare arbete med datainsamling, validering och integration. Dessutom hade det inneburit ytterligare belastning för vårt frontend team, som redan hade en väldigt stor mängd arbete att utföra. Vi bedömde därför att detta inte rymdes inom den återstående tiden.

Konsekvens: Backtesting ingår inte i slutleveransen. För fortsatt utveckling behöver strategi, datakälla, simuleringsantaganden och acceptanskriterier fastställas.

Kända brister och avgränsningar

Backtesting ingår inte i leveransen. Systemet saknar en färdig och integrerad C/C++-modul för att simulera hur en investeringsstrategi skulle ha utvecklats historiskt, och därmed kan lösningen inte användas för att jämföra strategiers historiska resultat. Avgränsningen grundades i osäkerheter kring omfattning, kunskapsbehov och tillgång till lämplig historisk data.

•Historisk valutaomräkning saknas i det befintliga FX-gränssnittet. FxConvert använder senast hämtade valutakurser och tar inget datum som argument. Gränssnittet kan därför inte användas för att räkna om historiska instrumentpriser med respektive dags valutakurs. Detta byggdes men vi valde att inte mergea in det i projektet eftersom att det primära användningsområdet för detta skulle ha varit i backtestmodulen, som vi beslutade att inte bygga.

•FX-modulen är beroende av en extern tjänst. Vid misslyckad hämtning kan tidigare cachade kurser användas, och ett lyckat anrop innebär därför inte alltid att en ny kurs har hämtats. Vi är beroende av ett externt API för att hämta in valutakurser, och om ingen cachad data finns tillgänglig samtidigt som det externa API:et inte kan nås, så kan vi inte beräkna några valutakurser.





Arbete med specialisternas feedforward:

Vi har inte fått någon specifik feedforward från specialisterna avseende C/C++-delarna. Därför finns inga ändringar inom dessa delar som vi kan koppla direkt till sådan återkoppling. Likaså fick vi aldrig något V1-kodbas att förbättra eller arbeta efter, utan all C/C++ kod i projektet är skapad av oss, med vår arkitektur, research och utifrån vår tolkning av de krav som stod specificerade av beställaren.
Vid en fortsatt utveckling skulle vi vilja få återkoppling på beräkningsmetoderna i riskmodulen, robustheten i FX-modulens datahantering, tillgång till pålitliga API:er och en rimlig omfattning för en framtida backtestmodul.
