package se.comerit.avanza.instrument;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.comerit.avanza.exception.ResourceConflictException;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.instrument.dto.InstrumentResponse;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;
import se.comerit.avanza.instrument.repository.InstrumentRepository;
import se.comerit.avanza.instrument.service.InstrumentService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstrumentServiceTest {

    @Mock
    private InstrumentRepository instrumentRepository;

    @InjectMocks
    private InstrumentService instrumentService;

    @Test
    void createShouldNormalizeAndSaveInstrument() {
        when(instrumentRepository.findByTickerIgnoreCase("AAPL"))
                .thenReturn(Optional.empty());

        instrumentService.create(
                " aapl ",
                " Apple Inc. ",
                InstrumentType.STOCK,
                Sector.UNKNOWN,
                " usd "
        );

        ArgumentCaptor<Instrument> captor = ArgumentCaptor.forClass(Instrument.class);
        verify(instrumentRepository).save(captor.capture());

        Instrument saved = captor.getValue();
        assertEquals("AAPL", saved.getTicker());
        assertEquals("Apple Inc.", saved.getName());
        assertEquals(InstrumentType.STOCK, saved.getInstrumentType());
        assertEquals(Sector.UNKNOWN, saved.getSector());
        assertEquals("USD", saved.getCurrency());
    }

    @Test
    void createShouldRejectDuplicateTicker() {
        Instrument existing = instrument(
                1,
                "AAPL",
                "Apple Inc.",
                InstrumentType.STOCK,
                Sector.UNKNOWN,
                "USD"
        );

        when(instrumentRepository.findByTickerIgnoreCase("AAPL"))
                .thenReturn(Optional.of(existing));

        ResourceConflictException exception = assertThrows(
                ResourceConflictException.class,
                () -> instrumentService.create(
                        " aapl ",
                        "Apple Inc.",
                        InstrumentType.STOCK,
                        Sector.UNKNOWN,
                        "USD"
                )
        );

        assertEquals("Instrument with ticker AAPL already exists", exception.getMessage());
        verify(instrumentRepository, never()).save(org.mockito.ArgumentMatchers.any(Instrument.class));
    }

    @Test
    void getByIdShouldReturnInstrument() {
        Instrument apple = instrument(
                1,
                "AAPL",
                "Apple Inc.",
                InstrumentType.STOCK,
                Sector.UNKNOWN,
                "USD"
        );

        when(instrumentRepository.findById(1)).thenReturn(Optional.of(apple));

        Instrument result = instrumentService.getById(1);

        assertSame(apple, result);
        verify(instrumentRepository).findById(1);
    }

    @Test
    void getByIdShouldThrowWhenInstrumentDoesNotExist() {
        when(instrumentRepository.findById(99)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> instrumentService.getById(99)
        );

        assertEquals("Instrument with id 99 not found", exception.getMessage());
    }

    @Test
    void getByTickerShouldNormalizeAndReturnInstrument() {
        Instrument apple = instrument(
                1,
                "AAPL",
                "Apple Inc.",
                InstrumentType.STOCK,
                Sector.TECHNOLOGY,
                "USD"
        );

        when(instrumentRepository.findByTickerIgnoreCase("AAPL")).thenReturn(Optional.of(apple));

        Instrument result = instrumentService.getByTicker(" aapl ");

        assertSame(apple, result);
        verify(instrumentRepository).findByTickerIgnoreCase("AAPL");
    }

    @Test
    void getByTickerShouldThrowWhenInstrumentDoesNotExist() {
        when(instrumentRepository.findByTickerIgnoreCase("MISSING")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> instrumentService.getByTicker("missing")
        );

        assertEquals("Instrument with ticker MISSING not found", exception.getMessage());
    }

    @Test
    void enumsShouldExposeFrontendLabelsWithoutChangingPersistedEnumNames() {
        assertEquals("Aktie", InstrumentType.STOCK.getDisplayName());
        assertEquals("Fond", InstrumentType.FUND.getDisplayName());
        assertEquals("Teknik", Sector.TECHNOLOGY.getDisplayName());
        assertEquals("Övrigt", Sector.UNKNOWN.getDisplayName());
    }

    @Test
    void getInstrumentByIdShouldReturnMappedResponse() {
        Instrument apple = instrument(
                1,
                "AAPL",
                "Apple Inc.",
                InstrumentType.STOCK,
                Sector.UNKNOWN,
                "USD"
        );

        when(instrumentRepository.findById(1)).thenReturn(Optional.of(apple));

        InstrumentResponse response = instrumentService.getInstrumentById(1);

        assertEquals(1, response.id());
        assertEquals("AAPL", response.ticker());
        assertEquals("Apple Inc.", response.name());
        assertEquals(InstrumentType.STOCK, response.instrumentType());
        assertEquals(Sector.UNKNOWN, response.sector());
        assertEquals("USD", response.currency());
    }

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
