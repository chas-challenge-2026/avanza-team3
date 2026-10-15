package se.comerit.avanza.pricehistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.pricehistory.model.InstrumentPrice;

import java.time.LocalDate;
import java.util.List;

public interface InstrumentPriceRepository extends JpaRepository<InstrumentPrice, Long> {
    List<InstrumentPrice> findByInstrumentIdAndDateBetweenOrderByDateAsc(
            Integer instrumentId, LocalDate from, LocalDate to);
}
