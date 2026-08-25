package se.comerit.avanza.holding.dto;

public class HoldingRequest {

    /*
    DTO = Data Transfer Object.

    Den används för information som skickas mellan
    frontend och backend.

    Vi skickar INTE hela Holding-entiteten direkt.

    Om frontend bara behöver skicka:
    - instrumentId
    - quantity
    - accountId

    så innehåller requesten bara dessa värden.

    Det gör API:t tydligare och minskar risken att klienten
    kan skicka eller ändra information som den inte ska kunna påverka.

    */

/*
Ett dto kan se ut så här och validering här ser ut som ovan name
package se.comerit.avanza.holding.dto;

public record HoldingRequest(
@NotNull <-validering
String name,
Long id
)

{}
*/
}
