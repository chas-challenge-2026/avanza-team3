package se.comerit.avanza.market;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.comerit.avanza.market.controller.MarketController;
import se.comerit.avanza.market.service.MarketDataService;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MarketControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MarketDataService marketDataService;

    @InjectMocks
    private MarketController marketController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(marketController).build();
    }

    @Test
    void getPriceShouldReturnTickerAndPrice() throws Exception {
        when(marketDataService.getPrice("aapl")).thenReturn(new BigDecimal("187.32"));

        mockMvc.perform(get("/api/market/price/aapl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("AAPL"))
                .andExpect(jsonPath("$.price").value(187.32));

        verify(marketDataService).getPrice("aapl");
    }

    @Test
    void getFxRateShouldReturnCurrencyPairAndRate() throws Exception {
        when(marketDataService.getEffectiveFxRate("usd", "sek")).thenReturn(new BigDecimal("10.45"));

        mockMvc.perform(get("/api/market/fx/usd/sek"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("SEK"))
                .andExpect(jsonPath("$.rate").value(10.45));

        verify(marketDataService).getEffectiveFxRate("usd", "sek");
    }

    @Test
    void convertShouldReturnConvertedAmount() throws Exception {
        when(marketDataService.convert(new BigDecimal("100.00"), "usd", "sek")).thenReturn(new BigDecimal("1045.00"));
        when(marketDataService.getEffectiveFxRate("usd", "sek")).thenReturn(new BigDecimal("10.45"));

        mockMvc.perform(get("/api/market/fx/convert")
                        .param("amount", "100.00")
                        .param("from", "usd")
                        .param("to", "sek"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.fromCurrency").value("USD"))
                .andExpect(jsonPath("$.toCurrency").value("SEK"))
                .andExpect(jsonPath("$.rate").value(10.45))
                .andExpect(jsonPath("$.convertedAmount").value(1045.00));

        verify(marketDataService).convert(new BigDecimal("100.00"), "usd", "sek");
        verify(marketDataService).getEffectiveFxRate("usd", "sek");
    }
}
