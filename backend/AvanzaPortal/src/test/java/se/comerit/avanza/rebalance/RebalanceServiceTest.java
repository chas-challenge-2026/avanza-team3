package se.comerit.avanza.rebalance;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.account.repository.AccountRepository;
import se.comerit.avanza.accountallocation.model.AccountAllocationTarget;
import se.comerit.avanza.accountallocation.model.AssetCategory;
import se.comerit.avanza.accountallocation.repository.AccountAllocationTargetRepository;
import se.comerit.avanza.exception.ResourceConflictException;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.holding.model.Holding;
import se.comerit.avanza.holding.repository.HoldingRepository;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.repository.InstrumentRepository;
import se.comerit.avanza.market.service.MarketDataService;
import se.comerit.avanza.portfolio.service.PortfolioValuationService;
import se.comerit.avanza.rebalance.dto.RebalanceConfirmRequest;
import se.comerit.avanza.rebalance.dto.RebalancePreviewRequest;
import se.comerit.avanza.rebalance.service.RebalanceCalculator;
import se.comerit.avanza.rebalance.service.RebalanceProposalTokenService;
import se.comerit.avanza.rebalance.service.RebalanceService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RebalanceServiceTest {
    private final AccountRepository accounts = mock(AccountRepository.class);
    private final HoldingRepository holdings = mock(HoldingRepository.class);
    private final AccountAllocationTargetRepository targets = mock(AccountAllocationTargetRepository.class);
    private final InstrumentRepository instruments = mock(InstrumentRepository.class);
    private final MarketDataService prices = mock(MarketDataService.class);
    private final PortfolioValuationService valuation = mock(PortfolioValuationService.class);
    private RebalanceService service;
    private Account account;
    private Instrument stock;

    @BeforeEach
    void setUp() {
        service = new RebalanceService(accounts, holdings, targets, instruments, prices, valuation,
                new RebalanceCalculator(), new RebalanceProposalTokenService(
                "test-secret-key-with-at-least-32-characters"));
        account = new Account(7, "ISK", "ISK 1", "SEK");
        ReflectionTestUtils.setField(account, "id", 11);
        account.setCashBalanceSek(new BigDecimal("1000.00"));
        stock = new Instrument("TEST", "Test stock", InstrumentType.STOCK, null, "SEK");
        ReflectionTestUtils.setField(stock, "id", 42);
    }

    private void validPortfolio() {
        when(accounts.findByIdAndUserId(11, 7)).thenReturn(Optional.of(account));
        when(targets.findByAccountIdOrderByAssetCategoryAsc(11))
                .thenReturn(List.of(new AccountAllocationTarget(account, AssetCategory.STOCK,
                        new BigDecimal("100.00"))));
        when(instruments.findAllById(List.of(42))).thenReturn(List.of(stock));
        when(prices.getPrice("TEST")).thenReturn(new BigDecimal("100.00"));
        when(prices.getEffectiveFxRate("SEK", "SEK")).thenReturn(BigDecimal.ONE);
        when(holdings.findAllByOwnedAccount(11, 7)).thenReturn(List.of());
    }

    @Test
    void previewIsReadOnlyAndUsesExistingInstrumentIds() {
        validPortfolio();
        var preview = service.preview(11, 7, new RebalancePreviewRequest(List.of(42)));
        assertTrue(preview.executable());
        assertNotNull(preview.proposalToken());
        assertEquals("BUY", preview.trades().getFirst().side());
        assertEquals(42, preview.trades().getFirst().instrumentId());
        assertEquals(new BigDecimal("1000.00"), preview.totalValueSek());
        assertEquals(1, preview.proposedHoldings().size());
        assertEquals(new BigDecimal("0"), preview.proposedHoldings().getFirst().currentQuantity());
        assertEquals(new BigDecimal("10"), preview.proposedHoldings().getFirst().proposedQuantity());
        verify(holdings, never()).save(any());
        verify(accounts, never()).save(any());
    }

    @Test
    void confirmAppliesTradeOnceAndPreservesRemainingCash() {
        validPortfolio();
        var preview = service.preview(11, 7, new RebalancePreviewRequest(List.of(42)));
        when(accounts.findOwnedAccountForUpdate(11, 7)).thenReturn(Optional.of(account));
        when(holdings.findAllByOwnedAccountForUpdate(11, 7)).thenReturn(List.of());
        var result = service.confirm(11, 7, new RebalanceConfirmRequest(preview.proposalToken()));
        assertEquals("EXECUTED", result.status());
        assertEquals(new BigDecimal("0.00"), account.getCashBalanceSek());
        assertEquals(1, account.getRebalanceVersion());
        verify(holdings).save(any(Holding.class));
        verify(accounts).save(account);
        assertThrows(ResourceConflictException.class,
                () -> service.confirm(11, 7, new RebalanceConfirmRequest(preview.proposalToken())));
    }

    @Test
    void changingAccountCashInvalidatesOldPreview() {
        validPortfolio();
        var preview = service.preview(11, 7, new RebalancePreviewRequest(List.of(42)));
        account.setCashBalanceSek(new BigDecimal("900.00"));
        when(accounts.findOwnedAccountForUpdate(11, 7)).thenReturn(Optional.of(account));
        when(holdings.findAllByOwnedAccountForUpdate(11, 7)).thenReturn(List.of());
        assertThrows(ResourceConflictException.class,
                () -> service.confirm(11, 7, new RebalanceConfirmRequest(preview.proposalToken())));
        verify(holdings, never()).save(any());
        verify(accounts, never()).save(any());
    }

    @Test
    void rejectsAccountNotOwnedBeforeLookingUpInstruments() {
        assertThrows(ResourceNotFoundException.class,
                () -> service.preview(11, 8, new RebalancePreviewRequest(List.of(42))));
        verifyNoInteractions(holdings, targets, instruments, prices);
    }
}
