package se.comerit.avanza.pricehistory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.comerit.avanza.exception.BusinessRuleException;
import se.comerit.avanza.exception.GlobalExceptionHandler;
import se.comerit.avanza.pricehistory.controller.HistoricalPriceController;
import se.comerit.avanza.pricehistory.controller.HistoricalPriceExceptionHandler;
import se.comerit.avanza.pricehistory.dto.PriceHistoryResponse;
import se.comerit.avanza.pricehistory.provider.HistoricalPriceProviderException;
import se.comerit.avanza.pricehistory.service.HistoricalPriceService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HistoricalPriceControllerTest {
    private static final String URL = "/api/price-history/instruments/1";
    private static final LocalDate FROM = LocalDate.of(2025, 1, 2);
    private static final LocalDate TO = LocalDate.of(2025, 1, 3);

    @Mock HistoricalPriceService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new HistoricalPriceController(service))
                .setControllerAdvice(new GlobalExceptionHandler(), new HistoricalPriceExceptionHandler())
                .build();
    }

    @Test
    void readsStoredHistory() throws Exception {
        when(service.getHistory(1, FROM, TO))
                .thenReturn(new PriceHistoryResponse(1, "AAPL", "STUB", List.of()));
        mvc.perform(get(URL).param("from", FROM.toString()).param("to", TO.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configuredProvider").value("STUB"))
                .andExpect(jsonPath("$.prices").isEmpty());
        verify(service, never()).importHistory(any(), any(), any());
    }

    @Test
    void providerFailureReturns503WithoutExposingDetails() throws Exception {
        when(service.importHistory(1, FROM, TO))
                .thenThrow(new HistoricalPriceProviderException("Sensitive upstream detail"));
        mvc.perform(post(URL + "/import").param("from", FROM.toString()).param("to", TO.toString()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Historical price provider is unavailable"));
    }

    @Test
    void invalidRangeUsesExistingBusinessRuleHandler() throws Exception {
        when(service.getHistory(1, TO, FROM)).thenThrow(new BusinessRuleException("Invalid range"));
        mvc.perform(get(URL).param("from", TO.toString()).param("to", FROM.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidOrMissingDatesReturn400BeforeCallingService() throws Exception {
        mvc.perform(get(URL).param("from", "invalid").param("to", TO.toString()))
                .andExpect(status().isBadRequest());
        mvc.perform(get(URL)).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
