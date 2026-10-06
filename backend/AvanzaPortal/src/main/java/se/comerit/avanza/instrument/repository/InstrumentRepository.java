package se.comerit.avanza.instrument.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.comerit.avanza.instrument.model.Instrument;

import java.util.List;
import java.util.Optional;

public interface InstrumentRepository extends JpaRepository<Instrument, Integer> {
    Optional<Instrument> findByTickerIgnoreCase(String ticker);

    List<Instrument> findAllByOrderByTickerAsc();
}
