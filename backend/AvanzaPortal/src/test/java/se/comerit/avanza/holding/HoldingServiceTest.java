package se.comerit.avanza.holding;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.account.service.AccountService;
import se.comerit.avanza.holding.dto.HoldingPatchRequest;
import se.comerit.avanza.holding.dto.HoldingResponse;
import se.comerit.avanza.holding.model.Holding;
import se.comerit.avanza.holding.repository.HoldingRepository;
import se.comerit.avanza.holding.service.HoldingService;
import se.comerit.avanza.market.service.MarketDataService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoldingServiceTest {

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private MarketDataService marketDataService;

    @InjectMocks
    private HoldingService holdingService;

    @Test
    void getHoldingsByUserIdShouldCalculateMarketValueAndPnlWithBigDecimal() {
        Holding holding = holdingWithAccount(
                31, 11, 7, "ISK", "Main ISK",
                "ERIC-B", "Ericsson B",
                new BigDecimal("10"), new BigDecimal("70.00"), "SEK"
        );

        when(holdingRepository.findByAccountUserIdOrderByAccountAccountTypeAscTickerAsc(7))
                .thenReturn(List.of(holding));
        when(marketDataService.getPrice("ERIC-B"))
                .thenReturn(new BigDecimal("74.20"));

        List<Map<String, Object>> result = holdingService.getHoldingsByUserId(7);

        assertEquals(1, result.size());
        Map<String, Object> row = result.getFirst();
        assertEquals(31, row.get("id"));
        assertEquals(11, row.get("account_id"));
        assertEquals("ERIC-B", row.get("ticker"));
        assertEquals("Ericsson B", row.get("instrument_name"));
        assertEquals(new BigDecimal("74.20"), row.get("currentPrice"));
        assertEquals(new BigDecimal("742.00"), row.get("marketValue"));
        assertEquals(new BigDecimal("42.00"), row.get("pnl"));
        assertEquals(new BigDecimal("6.00"), row.get("pnlPct"));
        assertEquals("ISK", row.get("account_type"));
        assertEquals("Main ISK", row.get("account_name"));

        verify(marketDataService).getPrice("ERIC-B");
    }

    @Test
    void paginatedGetHoldingsShouldUseRequestedPageSizeAndSort() {
        Holding holding = holdingWithAccount(
                31, 11, 7, "ISK", "Main ISK",
                "ERIC-B", "Ericsson B",
                new BigDecimal("2"), new BigDecimal("70.00"), "SEK"
        );

        when(holdingRepository.findByAccountUserId(eq(7), any(Pageable.class)))
                .thenAnswer(invocation -> new PageImpl<>(List.of(holding), invocation.getArgument(1), 1));
        when(marketDataService.getPrice("ERIC-B"))
                .thenReturn(new BigDecimal("74.20"));

        Page<Map<String, Object>> result = holdingService.getHoldingsByUserId(7, 2, 15);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(holdingRepository).findByAccountUserId(eq(7), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(2, pageable.getPageNumber());
        assertEquals(15, pageable.getPageSize());
        assertNotNull(pageable.getSort().getOrderFor("account.accountType"));
        assertNotNull(pageable.getSort().getOrderFor("ticker"));
        assertTrue(pageable.getSort().getOrderFor("account.accountType").isAscending());
        assertTrue(pageable.getSort().getOrderFor("ticker").isAscending());

        Map<String, Object> row = result.getContent().getFirst();
        assertEquals(new BigDecimal("148.40"), row.get("marketValue"));
        assertEquals(new BigDecimal("8.40"), row.get("pnl"));
        assertEquals(new BigDecimal("6.00"), row.get("pnlPct"));
        verify(marketDataService).getPrice("ERIC-B");
    }

    @Test
    void addHoldingShouldVerifyAccountOwnershipBeforeSaving() {
        Account account = new Account(7, "ISK", "Main ISK", "SEK");
        when(accountService.getAccountByIdAndUserId(11, 7)).thenReturn(account);

        holdingService.addHolding(
                7, 11, "ERIC-B", "Ericsson B",
                new BigDecimal("5"), new BigDecimal("71.50"), "SEK"
        );

        InOrder inOrder = inOrder(accountService, holdingRepository);
        inOrder.verify(accountService).getAccountByIdAndUserId(11, 7);

        ArgumentCaptor<Holding> holdingCaptor = ArgumentCaptor.forClass(Holding.class);
        inOrder.verify(holdingRepository).save(holdingCaptor.capture());
        Holding saved = holdingCaptor.getValue();
        assertEquals(11, saved.getAccountId());
        assertEquals("ERIC-B", saved.getTicker());
        assertEquals("Ericsson B", saved.getInstrumentName());
        assertEquals(new BigDecimal("5"), saved.getQuantity());
        assertEquals(new BigDecimal("71.50"), saved.getAvgBuyPrice());
        assertEquals("SEK", saved.getCurrency());
    }

    @Test
    void deleteHoldingShouldDeleteOnlyHoldingOwnedByUser() {
        Holding holding = new Holding(
                11, "ERIC-B", "Ericsson B",
                BigDecimal.ONE, new BigDecimal("70.00"), "SEK"
        );
        when(holdingRepository.findByIdAndAccountUserId(31, 7)).thenReturn(Optional.of(holding));

        holdingService.deleteHolding(31, 7);

        verify(holdingRepository).findByIdAndAccountUserId(31, 7);
        verify(holdingRepository).delete(holding);
    }

    @Test
    void deleteHoldingShouldRejectHoldingNotOwnedByUser() {
        when(holdingRepository.findByIdAndAccountUserId(31, 7)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> holdingService.deleteHolding(31, 7)
        );

        assertEquals("Holding not Found", exception.getMessage());
        verify(holdingRepository, never()).delete(any());
    }

    private Holding holdingWithAccount(
            Integer holdingId,
            Integer accountId,
            Integer userId,
            String accountType,
            String accountName,
            String ticker,
            String instrumentName,
            BigDecimal quantity,
            BigDecimal avgBuyPrice,
            String currency) {

        Account account = new Account(userId, accountType, accountName, "SEK");
        ReflectionTestUtils.setField(account, "id", accountId);

        Holding holding = new Holding(accountId, ticker, instrumentName, quantity, avgBuyPrice, currency);
        ReflectionTestUtils.setField(holding, "id", holdingId);
        ReflectionTestUtils.setField(holding, "account", account);
        return holding;
    }
}
