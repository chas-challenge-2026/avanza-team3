package se.comerit.avanza.portfolio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.holding.model.Holding;
import se.comerit.avanza.holding.repository.HoldingRepository;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;
import se.comerit.avanza.market.service.MarketDataService;
import se.comerit.avanza.portfolio.dto.ValuedHolding;
import se.comerit.avanza.portfolio.service.PortfolioValuationService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioValuationServiceTest {

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private MarketDataService marketDataService;

    @Test
    void valueShouldConvertUsdHoldingToSekAndCalculatePnlOnce() {
        PortfolioValuationService service = new PortfolioValuationService(
                holdingRepository,
                marketDataService
        );
        Holding holding = holding(
                31,
                11,
                7,
                "ISK",
                "Main ISK",
                101,
                "AAPL",
                "Apple Inc.",
                "USD",
                new BigDecimal("10"),
                new BigDecimal("80.00")
        );

        when(marketDataService.getPrice("AAPL")).thenReturn(new BigDecimal("100.00"));
        when(marketDataService.getEffectiveFxRate("USD", "SEK")).thenReturn(new BigDecimal("10.00"));

        ValuedHolding result = service.value(holding);

        assertEquals(new BigDecimal("100.00"), result.currentPrice());
        assertEquals(new BigDecimal("10.00000000"), result.fxRateToSek());
        assertEquals(new BigDecimal("10000.00"), result.marketValueSek());
        assertEquals(new BigDecimal("8000.00"), result.costBasisSek());
        assertEquals(new BigDecimal("2000.00"), result.pnlSek());
        assertEquals(new BigDecimal("25.00"), result.pnlPct());
        assertEquals("ISK", result.accountType());
        assertEquals("AAPL", result.ticker());

        verify(marketDataService).getPrice("AAPL");
        verify(marketDataService).getEffectiveFxRate("USD", "SEK");
    }

    @Test
    void getPortfolioValueSekShouldUseSameValuationForAllHoldings() {
        PortfolioValuationService service = new PortfolioValuationService(
                holdingRepository,
                marketDataService
        );
        Holding sekHolding = holding(
                31, 11, 7, "ISK", "Main ISK", 101,
                "ERIC-B", "Ericsson B", "SEK",
                new BigDecimal("10"), new BigDecimal("70.00")
        );
        Holding usdHolding = holding(
                32, 11, 7, "ISK", "Main ISK", 102,
                "AAPL", "Apple Inc.", "USD",
                new BigDecimal("2"), new BigDecimal("80.00")
        );

        when(holdingRepository.findByAccountUserIdOrderByAccountAccountTypeAscInstrumentTickerAsc(7))
                .thenReturn(List.of(sekHolding, usdHolding));
        when(marketDataService.getPrice("ERIC-B")).thenReturn(new BigDecimal("75.00"));
        when(marketDataService.getEffectiveFxRate("SEK", "SEK")).thenReturn(BigDecimal.ONE);
        when(marketDataService.getPrice("AAPL")).thenReturn(new BigDecimal("100.00"));
        when(marketDataService.getEffectiveFxRate("USD", "SEK")).thenReturn(new BigDecimal("10.00"));

        BigDecimal result = service.getPortfolioValueSek(7);

        assertEquals(new BigDecimal("2750.00"), result);
    }

    @Test
    void valueShouldRejectHoldingWithoutInstrument() {
        PortfolioValuationService service = new PortfolioValuationService(
                holdingRepository,
                marketDataService
        );
        Holding holding = new Holding(11, "OLD", "Legacy", BigDecimal.ONE, BigDecimal.ONE, "SEK");
        ReflectionTestUtils.setField(holding, "id", 99);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.value(holding)
        );

        assertEquals("Holding 99 is not linked to an instrument", exception.getMessage());
    }

    private Holding holding(
            Integer holdingId,
            Integer accountId,
            Integer userId,
            String accountType,
            String accountName,
            Integer instrumentId,
            String ticker,
            String instrumentName,
            String currency,
            BigDecimal quantity,
            BigDecimal avgBuyPrice) {

        Account account = new Account(userId, accountType, accountName, "SEK");
        ReflectionTestUtils.setField(account, "id", accountId);

        Instrument instrument = new Instrument(
                ticker,
                instrumentName,
                InstrumentType.STOCK,
                Sector.TECHNOLOGY,
                currency
        );
        ReflectionTestUtils.setField(instrument, "id", instrumentId);

        Holding holding = new Holding(accountId, instrument, quantity, avgBuyPrice);
        ReflectionTestUtils.setField(holding, "id", holdingId);
        ReflectionTestUtils.setField(holding, "account", account);
        return holding;
    }
}
