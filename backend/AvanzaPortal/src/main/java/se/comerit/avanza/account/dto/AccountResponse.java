package se.comerit.avanza.account.dto;

public record AccountResponse(
        Integer id,
        String accountType,
        String accountName,
        String currency
)
{}
