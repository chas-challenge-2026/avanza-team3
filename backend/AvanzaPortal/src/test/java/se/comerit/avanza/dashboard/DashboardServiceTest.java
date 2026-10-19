package se.comerit.avanza.dashboard;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import se.comerit.avanza.account.service.AccountService;
import se.comerit.avanza.alert.service.AlertService;
import se.comerit.avanza.dashboard.dto.DashboardView;
import se.comerit.avanza.dashboard.service.DashboardService;
import se.comerit.avanza.holding.service.HoldingService;
import se.comerit.avanza.market.service.MarketDataService;
import se.comerit.avanza.portfolio.dto.PortfolioAllocationResult;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;
import se.comerit.avanza.portfolio.dto.PortfolioAllocationRow;
import se.comerit.avanza.portfolio.dto.ValuedHolding;
import se.comerit.avanza.portfolio.service.PortfolioAllocationService;
import se.comerit.avanza.portfolio.service.PortfolioValuationService;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private AccountService accountService;

    @Mock
    private HoldingService holdingService;

    @Mock
    private AlertService alertService;

    @Mock
    private MarketDataService marketDataService;

    @Mock
    private PortfolioAllocationService portfolioAllocationService;

    @Mock
    private PortfolioValuationService portfolioValuationService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void dashboardShouldConsumeSharedHoldingValuationAndAllocationWithoutRepricingHoldings() {
        Map<String, Object> account = new LinkedHashMap<>();
        account.put("id", 11);
        account.put("user_id", 7);
        account.put("account_type", "ISK");
        account.put("account_name", "Main ISK");
        account.put("currency", "SEK");

        Map<String, Object> holding = valuedHoldingMap();

        when(accountService.getAccountMapsByUserId(7)).thenReturn(List.of(account));
        when(holdingService.getHoldingsByUserId(7, 0, 20)).thenReturn(
                new PageImpl<>(List.of(holding), PageRequest.of(0, 20), 1)
        );
        List<ValuedHolding> valuedHoldings = List.of(valuedHolding());
        when(portfolioValuationService.getValuedHoldingsByUserId(7)).thenReturn(valuedHoldings);
        when(portfolioAllocationService.getAccountTypeAllocation(7, valuedHoldings)).thenReturn(
                new PortfolioAllocationResult(
                        List.of(new PortfolioAllocationRow(
                                "ISK",
                                new BigDecimal("100.00"),
                                new BigDecimal("60.00"),
                                new BigDecimal("40.00"),
                                true
                        )),
                        new BigDecimal("10000.00"),
                        true
                )
        );
        when(alertService.getAlertsByUserId(7)).thenReturn(List.of());
        when(marketDataService.getEffectiveFxRate("USD", "SEK"))
                .thenReturn(new BigDecimal("10.00"));

        DashboardView result = dashboardService.getDashboardForUser(7, 0, 20);

        assertEquals(10000.00, result.totalPortfolioValue());
        assertEquals(new BigDecimal("10000.00"), result.accounts().getFirst().totalValueSek());
        assertEquals(new BigDecimal("10000.00"), result.holdings().content().getFirst().valueSek());
        assertEquals(new BigDecimal("2000.00"), result.holdings().content().getFirst().unrealizedReturn());
        assertEquals(new BigDecimal("25.00"), result.holdings().content().getFirst().unrealizedReturnPct());
        assertEquals(new BigDecimal("40.00"), result.allocationRows().getFirst().drift());
        assertTrue(result.anyDrift());

        verify(portfolioValuationService).getValuedHoldingsByUserId(7);
        verify(portfolioAllocationService).getAccountTypeAllocation(7, valuedHoldings);
        verify(marketDataService, never()).getPrice("AAPL");
    }

    private ValuedHolding valuedHolding() {
        return new ValuedHolding(
                31,
                11,
                "ISK",
                "Main ISK",
                101,
                "AAPL",
                "Apple Inc.",
                InstrumentType.STOCK,
                Sector.TECHNOLOGY,
                "USD",
                new BigDecimal("10"),
                new BigDecimal("80.00"),
                new BigDecimal("100.00"),
                new BigDecimal("10.00000000"),
                new BigDecimal("10000.00"),
                new BigDecimal("8000.00"),
                new BigDecimal("2000.00"),
                new BigDecimal("25.00")
        );
    }

    private Map<String, Object> valuedHoldingMap() {
        Map<String, Object> holding = new LinkedHashMap<>();
        holding.put("id", 31);
        holding.put("account_id", 11);
        holding.put("ticker", "AAPL");
        holding.put("instrument_name", "Apple Inc.");
        holding.put("quantity", new BigDecimal("10"));
        holding.put("avg_buy_price", new BigDecimal("80.00"));
        holding.put("currency", "USD");
        holding.put("account_type", "ISK");
        holding.put("account_name", "Main ISK");
        holding.put("currentPrice", new BigDecimal("100.00"));
        holding.put("marketValue", new BigDecimal("10000.00"));
        holding.put("pnl", new BigDecimal("2000.00"));
        holding.put("pnlPct", new BigDecimal("25.00"));
        return holding;
    }
}
