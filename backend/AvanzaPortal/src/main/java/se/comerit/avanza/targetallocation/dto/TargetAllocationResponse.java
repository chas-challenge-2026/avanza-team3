package se.comerit.avanza.targetallocation.dto;

import java.math.BigDecimal;

public record TargetAllocationResponse(
Integer id,
String accountType,
BigDecimal targetPercentage
)
{}
