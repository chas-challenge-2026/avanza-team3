package se.comerit.avanza.holding.controller;

public class HoldingController {

    /*
    Detta ska vara hela flödet
    Frontend / HTTP-request
        ↓
    Controller
    Tar emot requesten och skickar vidare
            ↓
    Service
    Här ligger programmets logik och regler
            ↓
    Repository
    Pratar med databasen
            ↓
    Postgres

    och tillbaka:

        Postgres
       ↑
    Repository
       ↑
    Service
       ↑
    Controller
       ↑
    DTO / JSON / View
       ↑
    Frontend
     */

        /*
    Controller = ingången från frontend/webben.

    Exempel:
    GET  /holdings       -> hämta holdings
    POST /holdings       -> skapa en holding
    DELETE /holdings/123 -> ta bort en holding

    Controllern ska helst INTE innehålla:
    - SQL
    - beräkningar
    - affärsregler

    Den tar emot en request, anropar rätt service
    och returnerar ett svar.
    Här under är ett exempel från ett annat projekt


    @GetMapping("/grape-similarities")
    public List<GrapeSimilarityResponse> grapeSimilarities(
            @RequestParam String grape,
            @RequestParam(defaultValue = "6") int limit
    ) {
        return grapeSimilarityService.similarTo(grape, limit);
    }
    */
}
