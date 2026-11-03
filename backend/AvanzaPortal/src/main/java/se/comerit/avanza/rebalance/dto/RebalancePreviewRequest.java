package se.comerit.avanza.rebalance.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RebalancePreviewRequest(
        @NotEmpty @Size(max = 50) List<@NotNull @Positive Integer> allowedInstrumentIds
) {}
