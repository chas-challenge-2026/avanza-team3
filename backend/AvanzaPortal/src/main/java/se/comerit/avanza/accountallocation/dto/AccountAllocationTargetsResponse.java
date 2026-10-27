package se.comerit.avanza.accountallocation.dto;

import java.util.List;

public record AccountAllocationTargetsResponse(
        Integer accountId,
        List<AccountAllocationTargetResponse> targets
) {}
