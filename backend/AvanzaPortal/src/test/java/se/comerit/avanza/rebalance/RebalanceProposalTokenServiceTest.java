package se.comerit.avanza.rebalance;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.comerit.avanza.exception.BusinessRuleException;
import se.comerit.avanza.rebalance.service.RebalanceProposalTokenService;
import static org.junit.jupiter.api.Assertions.*;

class RebalanceProposalTokenServiceTest {
    private final RebalanceProposalTokenService tokens =
            new RebalanceProposalTokenService("test-secret-key-with-at-least-32-characters");

    @Test
    void signedTokenBindsUserAccountVersionAndSortedInstrumentIds() {
        var token = tokens.issue(7, 11, 3, "snapshotHash", List.of(9, 2));
        var proposal = tokens.verify(token, 7, 11);
        assertEquals(3, proposal.version());
        assertEquals("snapshotHash", proposal.snapshotHash());
        assertEquals(List.of(2, 9), proposal.instrumentIds());
        assertThrows(BusinessRuleException.class, () -> tokens.verify(token, 8, 11));
        assertThrows(BusinessRuleException.class, () -> tokens.verify(token, 7, 12));
        assertThrows(BusinessRuleException.class, () -> tokens.verify(token + "a", 7, 11));
    }
}
