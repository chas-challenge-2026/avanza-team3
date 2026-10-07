package se.comerit.avanza.holding;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.account.service.AccountService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HoldingSummaryServiceTest {

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
    void summaryShouldUseAllFilteredHoldingsWithoutPagination() {
        Account account = new Account(7, "ISK", "Main ISK", "SEK");
        ReflectionTestUtils.setField(account, "id", 11);

        Instrument instrument = new Instrument(
                "ERIC-B",
                "Ericsson B",
                InstrumentType.STOCK,
                Sector.UNKNOWN,
                "SEK"
        );
        ReflectionTestUtils.setField(instrument, "id", 101);

        Holding holding = new Holding(
                11,
                instrument,
                new BigDecimal("10"),
                new BigDecimal("70.00")
        );
        ReflectionTestUtils.setField(holding, "id", 31);
        ReflectionTestUtils.setField(holding, "account", account);

        when(holdingRepository.findAllFilteredByUserId(7, 11, InstrumentType.STOCK))
                .thenReturn(List.of(holding));
        when(marketDataService.getPrice("ERIC-B"))
                .thenReturn(new BigDecimal("74.20"));

        List<Map<String, Object>> result = holdingService.getHoldingsSummary(
                7,
                11,
                InstrumentType.STOCK
        );

        assertEquals(1, result.size());
        assertEquals("ERIC-B", result.getFirst().get("ticker"));
        assertEquals(new BigDecimal("742.00"), result.getFirst().get("marketValue"));
        assertEquals(new BigDecimal("42.00"), result.getFirst().get("pnl"));

        verify(holdingRepository).findAllFilteredByUserId(7, 11, InstrumentType.STOCK);
        verify(marketDataService).getPrice("ERIC-B");
    }
}
