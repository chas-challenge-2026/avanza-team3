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
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.account.service.AccountService;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.holding.dto.HoldingPatchRequest;
import se.comerit.avanza.holding.dto.HoldingResponse;
import se.comerit.avanza.holding.model.Holding;
import se.comerit.avanza.holding.repository.HoldingRepository;
import se.comerit.avanza.holding.service.HoldingService;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;
import se.comerit.avanza.instrument.service.InstrumentService;
import se.comerit.avanza.market.service.MarketDataService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoldingServiceTest {

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private MarketDataService marketDataService;

    @Mock
    private InstrumentService instrumentService;

    @InjectMocks
    private HoldingService holdingService;

    @Test
    void getHoldingsByUserIdShouldCalculateMarketValueAndPnlWithBigDecimal() {
        Holding holding = holdingWithAccount(
                31, 11, 7, "ISK", "Main ISK",
                101, "ERIC-B", "Ericsson B",
                new BigDecimal("10"), new BigDecimal("70.00"), "SEK"
        );

        when(holdingRepository.findByAccountUserIdOrderByAccountAccountTypeAscInstrumentTickerAsc(7))
                .thenReturn(List.of(holding));
        when(marketDataService.getPrice("ERIC-B"))
                .thenReturn(new BigDecimal("74.20"));

        List<Map<String, Object>> result = holdingService.getHoldingsByUserId(7);

        assertEquals(1, result.size());
        Map<String, Object> row = result.getFirst();
        assertEquals(31, row.get("id"));
        assertEquals(11, row.get("account_id"));
        assertEquals(101, row.get("instrument_id"));
        assertEquals("ERIC-B", row.get("ticker"));
        assertEquals("Ericsson B", row.get("instrument_name"));
        assertEquals(InstrumentType.STOCK, row.get("instrument_type"));
        assertEquals(Sector.UNKNOWN, row.get("sector"));
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
                101, "ERIC-B", "Ericsson B",
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
        assertNotNull(pageable.getSort().getOrderFor("instrument.ticker"));
        assertTrue(pageable.getSort().getOrderFor("account.accountType").isAscending());
        assertTrue(pageable.getSort().getOrderFor("instrument.ticker").isAscending());

        Map<String, Object> row = result.getContent().getFirst();
        assertEquals(new BigDecimal("148.40"), row.get("marketValue"));
        assertEquals(new BigDecimal("8.40"), row.get("pnl"));
        assertEquals(new BigDecimal("6.00"), row.get("pnlPct"));
        verify(marketDataService).getPrice("ERIC-B");
    }

    @Test
    void filteredPaginatedGetHoldingsShouldPassAccountAndInstrumentTypeToRepository() {
        Holding holding = holdingWithAccount(
                31, 11, 7, "ISK", "Main ISK",
                101, "ERIC-B", "Ericsson B",
                new BigDecimal("2"), new BigDecimal("70.00"), "SEK"
        );

        when(holdingRepository.findFilteredByUserId(
                eq(7),
                eq(11),
                eq(InstrumentType.STOCK),
                any(Pageable.class)
        )).thenAnswer(invocation -> new PageImpl<>(List.of(holding), invocation.getArgument(3), 1));

        when(marketDataService.getPrice("ERIC-B"))
                .thenReturn(new BigDecimal("74.20"));

        Page<Map<String, Object>> result = holdingService.getHoldingsByUserId(
                7,
                0,
                20,
                11,
                InstrumentType.STOCK
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(holdingRepository).findFilteredByUserId(
                eq(7),
                eq(11),
                eq(InstrumentType.STOCK),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
        assertNotNull(pageable.getSort().getOrderFor("account.accountType"));
        assertNotNull(pageable.getSort().getOrderFor("instrument.ticker"));
        assertEquals(1, result.getTotalElements());
        assertEquals("ERIC-B", result.getContent().getFirst().get("ticker"));
    }

    @Test
    void addHoldingShouldVerifyAccountAndInstrumentBeforeSaving() {
        Account account = new Account(7, "ISK", "Main ISK", "SEK");
        Instrument instrument = instrument(
                101,
                "ERIC-B",
                "Ericsson B",
                InstrumentType.STOCK,
                Sector.UNKNOWN,
                "SEK"
        );

        when(accountService.getAccountByIdAndUserId(11, 7)).thenReturn(account);
        when(instrumentService.getById(101)).thenReturn(instrument);

        holdingService.addHolding(
                7, 11, 101,
                new BigDecimal("5"), new BigDecimal("71.50")
        );

        InOrder inOrder = inOrder(accountService, instrumentService, holdingRepository);
        inOrder.verify(accountService).getAccountByIdAndUserId(11, 7);
        inOrder.verify(instrumentService).getById(101);

        ArgumentCaptor<Holding> holdingCaptor = ArgumentCaptor.forClass(Holding.class);
        inOrder.verify(holdingRepository).save(holdingCaptor.capture());
        Holding saved = holdingCaptor.getValue();
        assertEquals(11, saved.getAccountId());
        assertSame(instrument, saved.getInstrument());
        assertEquals("ERIC-B", saved.getTicker());
        assertEquals("Ericsson B", saved.getInstrumentName());
        assertEquals(new BigDecimal("5"), saved.getQuantity());
        assertEquals(new BigDecimal("71.50"), saved.getAvgBuyPrice());
        assertEquals("SEK", saved.getCurrency());
    }

    @Test
    void deleteHoldingShouldDeleteOnlyHoldingOwnedByUser() {
        Holding holding = holdingWithAccount(
                31, 11, 7, "ISK", "Main ISK", 101, "ERIC-B", "Ericsson B",
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

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> holdingService.deleteHolding(31, 7)
        );

        assertEquals("Holding not found", exception.getMessage());
        verify(holdingRepository, never()).delete(any());
    }

    @Test
    void getHoldingByIdShouldReturnHoldingOwnedByUserWithCalculatedValues() {
        Holding holding = holdingWithAccount(
                31, 11, 7, "ISK", "Main ISK",
                101, "ERIC-B", "Ericsson B",
                new BigDecimal("10"), new BigDecimal("70.00"), "SEK"
        );

        when(holdingRepository.findByIdAndAccountUserId(31, 7))
                .thenReturn(Optional.of(holding));
        when(marketDataService.getPrice("ERIC-B"))
                .thenReturn(new BigDecimal("74.20"));

        HoldingResponse response = holdingService.getHoldingById(31, 7);

        assertEquals(31, response.id());
        assertEquals(11, response.accountId());
        assertEquals(101, response.instrumentId());
        assertEquals("ERIC-B", response.ticker());
        assertEquals("Ericsson B", response.instrumentName());
        assertEquals(InstrumentType.STOCK, response.instrumentType());
        assertEquals(Sector.UNKNOWN, response.sector());
        assertEquals(new BigDecimal("10"), response.quantity());
        assertEquals(new BigDecimal("70.00"), response.avgBuyPrice());
        assertEquals("SEK", response.currency());
        assertEquals(new BigDecimal("74.20"), response.currentPrice());
        assertEquals(new BigDecimal("742.00"), response.marketValueSek());
        assertEquals(new BigDecimal("42.00"), response.pnlSek());
        assertEquals(new BigDecimal("6.00"), response.pnlPct());

        verify(holdingRepository).findByIdAndAccountUserId(31, 7);
        verify(marketDataService).getPrice("ERIC-B");
    }

    @Test
    void getHoldingByIdShouldRejectHoldingNotOwnedByUserWithoutRequestingMarketPrice() {
        when(holdingRepository.findByIdAndAccountUserId(31, 7))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> holdingService.getHoldingById(31, 7)
        );

        verifyNoInteractions(marketDataService);
    }

    @Test
    void updateHoldingShouldOnlyChangeFieldsIncludedInPatch() {
        Holding holding = holdingWithAccount(
                31, 11, 7, "ISK", "Main ISK",
                101, "ERIC-B", "Ericsson B",
                new BigDecimal("10"), new BigDecimal("70.00"), "SEK"
        );

        when(holdingRepository.findByIdAndAccountUserId(31, 7))
                .thenReturn(Optional.of(holding));
        when(holdingRepository.save(any(Holding.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(marketDataService.getPrice("ERIC-B"))
                .thenReturn(new BigDecimal("74.20"));

        HoldingPatchRequest request = new HoldingPatchRequest(
                new BigDecimal("15"),
                null
        );

        HoldingResponse response = holdingService.updateHolding(31, 7, request);

        assertEquals(new BigDecimal("15"), response.quantity());
        assertEquals("ERIC-B", response.ticker());
        assertEquals("Ericsson B", response.instrumentName());
        assertEquals(new BigDecimal("70.00"), response.avgBuyPrice());
        assertEquals("SEK", response.currency());
        assertEquals(new BigDecimal("1113.00"), response.marketValueSek());
        assertEquals(new BigDecimal("63.00"), response.pnlSek());
        assertEquals(new BigDecimal("6.00"), response.pnlPct());

        verify(holdingRepository).save(holding);
        verify(marketDataService).getPrice("ERIC-B");
    }

    @Test
    void updateHoldingShouldRejectHoldingNotOwnedByUserWithoutSavingOrRequestingMarketPrice() {
        when(holdingRepository.findByIdAndAccountUserId(31, 7))
                .thenReturn(Optional.empty());

        HoldingPatchRequest request = new HoldingPatchRequest(
                new BigDecimal("15"), null
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> holdingService.updateHolding(31, 7, request)
        );

        verify(holdingRepository, never()).save(any());
        verifyNoInteractions(marketDataService);
    }

    @Test
    void addHoldingShouldNotSaveWhenAccountOwnershipCheckFails() {
        when(accountService.getAccountByIdAndUserId(11, 7))
                .thenThrow(new ResourceNotFoundException("Account not found"));

        assertThrows(
                ResourceNotFoundException.class,
                () -> holdingService.addHolding(
                        7, 11, 101,
                        new BigDecimal("5"), new BigDecimal("71.50")
                )
        );

        verifyNoInteractions(instrumentService);
        verify(holdingRepository, never()).save(any());
    }

    private Holding holdingWithAccount(
            Integer holdingId,
            Integer accountId,
            Integer userId,
            String accountType,
            String accountName,
            Integer instrumentId,
            String ticker,
            String instrumentName,
            BigDecimal quantity,
            BigDecimal avgBuyPrice,
            String currency) {

        Account account = new Account(userId, accountType, accountName, "SEK");
        ReflectionTestUtils.setField(account, "id", accountId);

        Instrument instrument = instrument(
                instrumentId,
                ticker,
                instrumentName,
                InstrumentType.STOCK,
                Sector.UNKNOWN,
                currency
        );

        Holding holding = new Holding(accountId, instrument, quantity, avgBuyPrice);
        ReflectionTestUtils.setField(holding, "id", holdingId);
        ReflectionTestUtils.setField(holding, "account", account);
        return holding;
    }

    private Instrument instrument(
            Integer id,
            String ticker,
            String name,
            InstrumentType instrumentType,
            Sector sector,
            String currency) {

        Instrument instrument = new Instrument(
                ticker,
                name,
                instrumentType,
                sector,
                currency
        );
        ReflectionTestUtils.setField(instrument, "id", id);
        return instrument;
    }
}
