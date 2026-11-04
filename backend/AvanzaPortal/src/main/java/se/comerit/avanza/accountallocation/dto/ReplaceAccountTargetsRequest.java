package se.comerit.avanza.accountallocation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReplaceAccountTargetsRequest(
        @NotEmpty
        List<@Valid TargetItemRequest> targets
) { }
