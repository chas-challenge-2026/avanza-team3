package se.comerit.avanza.rebalance;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.account.repository.AccountRepository;
import se.comerit.avanza.accountallocation.model.AccountAllocationTarget;
import se.comerit.avanza.accountallocation.model.AssetCategory;
import se.comerit.avanza.accountallocation.repository.AccountAllocationTargetRepository;
import se.comerit.avanza.accountallocation.service.AccountAllocationOverviewService;
import se.comerit.avanza.alert.service.AlertService;
import se.comerit.avanza.exception.ResourceConflictException;
import se.comerit.avanza.holding.model.Holding;
import se.comerit.avanza.holding.repository.HoldingRepository;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.instrument.repository.InstrumentRepository;
import se.comerit.avanza.rebalance.dto.RebalanceConfirmRequest;
import se.comerit.avanza.rebalance.dto.RebalancePreviewRequest;
import se.comerit.avanza.rebalance.service.RebalanceService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Opt-in PostgreSQL integration test. Requires a migrated isolated database (V1..V9),
 * seeded demo user id 1 and JWT_SECRET. Run with REBALANCE_PG_TEST=true.
 * Use a disposable database: changes are rolled back when the test finishes.
 */
@SpringBootTest
@Tag("postgres-integration")
@EnabledIfEnvironmentVariable(named = "REBALANCE_PG_TEST", matches = "true")
@Transactional
class RebalancePostgresIntegrationTest {
    private static final int SEEDED_USER_ID = 1;

    @Autowired private RebalanceService rebalance;
    @Autowired private AccountRepository accounts;
    @Autowired private HoldingRepository holdings;
    @Autowired private InstrumentRepository instruments;
    @Autowired private AccountAllocationTargetRepository targets;
    @Autowired private AccountAllocationOverviewService overview;
    @Autowired private AlertService alerts;
    @Autowired private EntityManager entityManager;

    @BeforeEach
    void authenticate() {
        var auth = new UsernamePasswordAuthenticationToken("anna@example.com", null, List.of());
        auth.setDetails(SEEDED_USER_ID);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void previewThenConfirmUpdatesHoldingsAndResolvesAccountAlerts() {
        Instrument stock = instruments.findByTickerIgnoreCase("ERIC-B").orElseThrow();
        Instrument fund = instruments.findByTickerIgnoreCase("FND-SE").orElseThrow();
        Account account = accounts.saveAndFlush(new Account(SEEDED_USER_ID, "ISK", "Rebalance integration demo", "SEK"));
        var stockHolding = holdings.save(new Holding(account.getId(), stock,
                new BigDecimal("10"), new BigDecimal("65.00")));
        var fundHolding = holdings.save(new Holding(account.getId(), fund,
                new BigDecimal("1"), new BigDecimal("100.00")));
        targets.save(new AccountAllocationTarget(account, AssetCategory.STOCK, new BigDecimal("50.00")));
        targets.save(new AccountAllocationTarget(account, AssetCategory.FUND, new BigDecimal("50.00")));
        entityManager.flush();
        entityManager.clear();

        var before = overview.getAllocation(account.getId(), SEEDED_USER_ID);
        assertEquals(2, before.categories().stream().filter(c -> c.targetPct().signum() > 0).count());
        assertEquals(2, alerts.getLiveAlertsByUserId(SEEDED_USER_ID).stream()
                .filter(a -> a.accountId().equals(account.getId())).count());

        var request = new RebalancePreviewRequest(List.of(stock.getId(), fund.getId()));
        var preview = rebalance.preview(account.getId(), SEEDED_USER_ID, request);
        assertTrue(preview.executable());
        assertNotNull(preview.proposalToken());
        assertEquals(2, preview.proposedHoldings().size());
        assertEquals(2, preview.trades().size());
        // Preview must not persist changes.
        assertEquals(0, holdings.findById(stockHolding.getId()).orElseThrow()
                .getQuantity().compareTo(new BigDecimal("10")));
        assertEquals(0, holdings.findById(fundHolding.getId()).orElseThrow()
                .getQuantity().compareTo(BigDecimal.ONE));

        var confirmed = rebalance.confirm(account.getId(), SEEDED_USER_ID,
                new RebalanceConfirmRequest(preview.proposalToken()));
        assertEquals("EXECUTED", confirmed.status());
        entityManager.flush();
        entityManager.clear();
        assertEquals(1, accounts.findById(account.getId()).orElseThrow().getRebalanceVersion());
        assertTrue(holdings.findById(stockHolding.getId()).orElseThrow()
                .getQuantity().compareTo(new BigDecimal("10")) < 0);
        Holding updatedFund = holdings.findById(fundHolding.getId()).orElseThrow();
        assertTrue(updatedFund.getQuantity().compareTo(BigDecimal.ONE) > 0);
        // BUY into an existing holding must update its weighted native-currency buy price.
        assertTrue(updatedFund.getAvgBuyPrice().compareTo(new BigDecimal("100.00")) > 0);
        assertTrue(updatedFund.getAvgBuyPrice().compareTo(new BigDecimal("125.00")) < 0);
        var after = overview.getAllocation(account.getId(), SEEDED_USER_ID);
        assertTrue(after.categories().stream()
                .filter(c -> c.category() == AssetCategory.STOCK || c.category() == AssetCategory.FUND)
                .allMatch(c -> c.differencePctPoints().abs().compareTo(new BigDecimal("5.00")) <= 0));
        assertEquals(0, alerts.getLiveAlertsByUserId(SEEDED_USER_ID).stream()
                .filter(a -> a.accountId().equals(account.getId())).count());
        assertThrows(ResourceConflictException.class,
                () -> rebalance.confirm(account.getId(), SEEDED_USER_ID,
                        new RebalanceConfirmRequest(preview.proposalToken())));
    }
}
