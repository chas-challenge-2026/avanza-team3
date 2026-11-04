package se.comerit.avanza.rebalance.dto;

import jakarta.validation.constraints.NotBlank;
public record RebalanceConfirmRequest(@NotBlank String proposalToken) {}
