package se.comerit.avanza.pricehistory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import se.comerit.avanza.exception.BusinessRuleException;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;
import se.comerit.avanza.instrument.repository.InstrumentRepository;
import se.comerit.avanza.instrument.service.InstrumentService;
import se.comerit.avanza.pricehistory.dto.HistoricalPricePoint;
import se.comerit.avanza.pricehistory.provider.HistoricalPriceProvider;
import se.comerit.avanza.pricehistory.provider.HistoricalPriceProviderException;
import se.comerit.avanza.pricehistory.repository.InstrumentPriceRepository;
import se.comerit.avanza.pricehistory.service.HistoricalPriceService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest
@ActiveProfiles("test")
@Import({HistoricalPriceService.class, InstrumentService.class})
class HistoricalPriceServiceTest {
    private static final LocalDate FROM = LocalDate.of(2025, 1, 2);
    private static final LocalDate TO = LocalDate.of(2025, 1, 3);

    @Autowired HistoricalPriceService service;
    @Autowired InstrumentRepository instruments;
    @Autowired InstrumentPriceRepository prices;
    @MockBean HistoricalPriceProvider provider;

    private Instrument instrument;

    @BeforeEach
    void setUp() {
        instrument = instruments.saveAndFlush(new Instrument(
                "AAPL", "Apple", InstrumentType.STOCK, Sector.UNKNOWN, "USD"));
    }

    @Test
    void importSortsAndReimportUpdatesWithoutDuplicates() {
        when(provider.source()).thenReturn("TEST");
        when(provider.fetchPrices(instrument, FROM, TO)).thenReturn(
                List.of(point(TO, "200"), point(FROM, "190")),
                List.of(point(TO, "201")));

        var first = service.importHistory(instrument.getId(), FROM, TO);
        assertEquals(List.of(FROM, TO), first.prices().stream().map(HistoricalPricePoint::date).toList());

        service.importHistory(instrument.getId(), FROM, TO);
        prices.flush();
        var result = service.getHistory(instrument.getId(), FROM, TO);
        assertEquals(2, prices.count());
        assertEquals(0, new BigDecimal("201").compareTo(result.prices().get(1).close()));
        assertNull(result.prices().get(1).adjustedClose());
    }

    @Test
    void readsDoNotCallProviderAndRespectDateBounds() {
        when(provider.source()).thenReturn("TEST");
        when(provider.fetchPrices(instrument, FROM, TO)).thenReturn(List.of(point(FROM, "190"), point(TO, "200")));
        service.importHistory(instrument.getId(), FROM, TO);
        clearInvocations(provider);

        var result = service.getHistory(instrument.getId(), TO, TO);
        assertEquals(List.of(TO), result.prices().stream().map(HistoricalPricePoint::date).toList());
        verify(provider, never()).fetchPrices(any(), any(), any());
    }

    @Test
    void rejectsInvalidRangeAndUnknownInstrumentBeforeFetching() {
        assertThrows(BusinessRuleException.class, () -> service.importHistory(instrument.getId(), TO, FROM));
        assertThrows(ResourceNotFoundException.class, () -> service.importHistory(-1, FROM, TO));
        verifyNoInteractions(provider);
    }

    @Test
    void rejectsDuplicateDatesBeforeWriting() {
        when(provider.fetchPrices(instrument, FROM, TO)).thenReturn(
                List.of(point(FROM, "190"), point(FROM, "191")));
        assertThrows(HistoricalPriceProviderException.class,
                () -> service.importHistory(instrument.getId(), FROM, TO));
        assertEquals(0, prices.count());
    }

    @Test
    void rejectsInvalidPricesCurrenciesAndDatesBeforeWriting() {
        var invalidPoints = List.of(point(FROM, "0"), point(FROM, "-1"),
                point(FROM, "0.000000001"), point(TO.plusDays(1), "200"),
                new HistoricalPricePoint(FROM, BigDecimal.ONE, null, "SEK"));
        for (var point : invalidPoints) {
            when(provider.fetchPrices(instrument, FROM, TO)).thenReturn(List.of(point));
            assertThrows(HistoricalPriceProviderException.class,
                    () -> service.importHistory(instrument.getId(), FROM, TO));
        }
        assertEquals(0, prices.count());
    }

    @Test
    void upstreamFailureDoesNotOverwriteStoredData() {
        when(provider.source()).thenReturn("TEST");
        when(provider.fetchPrices(instrument, FROM, TO)).thenReturn(List.of(point(FROM, "190")));
        service.importHistory(instrument.getId(), FROM, TO);
        when(provider.fetchPrices(instrument, FROM, TO))
                .thenThrow(new HistoricalPriceProviderException("Unavailable"));
        assertThrows(HistoricalPriceProviderException.class,
                () -> service.importHistory(instrument.getId(), FROM, TO));
        assertEquals(1, prices.count());
    }

    private HistoricalPricePoint point(LocalDate date, String close) {
        return new HistoricalPricePoint(date, new BigDecimal(close), null, "USD");
    }
}
