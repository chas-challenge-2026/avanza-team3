package se.comerit.avanza.instrument;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import se.comerit.avanza.instrument.controller.InstrumentController;
import se.comerit.avanza.instrument.dto.InstrumentResponse;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;
import se.comerit.avanza.instrument.service.InstrumentService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InstrumentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InstrumentService instrumentService;

    @InjectMocks
    private InstrumentController instrumentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(instrumentController).build();
    }

    @Test
    void listInstrumentsShouldReturnAllInstruments() throws Exception {
        when(instrumentService.getAllInstruments()).thenReturn(List.of(
                new InstrumentResponse(
                        1,
                        "AAPL",
                        "Apple Inc.",
                        InstrumentType.STOCK,
                        Sector.UNKNOWN,
                        "USD"
                ),
                new InstrumentResponse(
                        2,
                        "ERIC-B",
                        "Ericsson B",
                        InstrumentType.STOCK,
                        Sector.UNKNOWN,
                        "SEK"
                )
        ));

        mockMvc.perform(get("/api/instruments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].ticker").value("AAPL"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].ticker").value("ERIC-B"));

        verify(instrumentService).getAllInstruments();
    }
    @Test
    void addInstrumentShouldRejectInvalidCurrencyBeforeCallingService() throws Exception {
        String body = """
                {
                  "ticker": "AAPL",
                  "name": "Apple Inc.",
                  "instrumentType": "STOCK",
                  "sector": "UNKNOWN",
                  "currency": "US"
                }
                """;

        mockMvc.perform(post("/api/instruments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(instrumentService);
    }

}
