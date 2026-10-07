package se.comerit.avanza.instrument;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.comerit.avanza.instrument.dto.InstrumentResponse;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;
import se.comerit.avanza.instrument.repository.InstrumentRepository;
import se.comerit.avanza.instrument.service.InstrumentService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstrumentServiceTest {

    @Mock
    private InstrumentRepository instrumentRepository;

    @InjectMocks
    private InstrumentService instrumentService;

    @Test
    void getAllInstrumentsShouldReturnRepositoryOrderAsResponses() {
        Instrument apple = instrument(
                1,
                "AAPL",
                "Apple Inc.",
                InstrumentType.STOCK,
                Sector.UNKNOWN,
                "USD"
        );
        Instrument ericsson = instrument(
                2,
                "ERIC-B",
                "Ericsson B",
                InstrumentType.STOCK,
                Sector.UNKNOWN,
                "SEK"
        );

        when(instrumentRepository.findAllByOrderByTickerAsc())
                .thenReturn(List.of(apple, ericsson));

        List<InstrumentResponse> result = instrumentService.getAllInstruments();

        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).ticker());
        assertEquals("Apple Inc.", result.get(0).name());
        assertEquals("ERIC-B", result.get(1).ticker());
        assertEquals("Ericsson B", result.get(1).name());

        verify(instrumentRepository).findAllByOrderByTickerAsc();
    }

    private Instrument instrument(
            Integer id,
            String ticker,
            String name,
            InstrumentType instrumentType,
            Sector sector,
            String currency
    ) {
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
