package se.comerit.avanza.pricehistory.model;

import jakarta.persistence.*;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.pricehistory.dto.HistoricalPricePoint;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "instrument_prices", uniqueConstraints =
        @UniqueConstraint(name = "uk_instrument_price_date", columnNames = {"instrument_id", "price_date"}))
public class InstrumentPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instrument instrument;

    @Column(name = "price_date", nullable = false)
    private LocalDate date;

    @Column(name = "close_price", nullable = false, precision = 24, scale = 8)
    private BigDecimal close;

    @Column(name = "adjusted_close", precision = 24, scale = 8)
    private BigDecimal adjustedClose;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 50)
    private String source;

    protected InstrumentPrice() {}

    public InstrumentPrice(Instrument instrument, HistoricalPricePoint point, String source) {
        this.instrument = instrument;
        this.date = point.date();
        update(point, source);
    }

    public void update(HistoricalPricePoint point, String source) {
        this.close = point.close();
        this.adjustedClose = point.adjustedClose();
        this.currency = point.currency();
        this.source = source;
    }

    public LocalDate getDate() { return date; }
    public String getSource() { return source; }

    public HistoricalPricePoint toPoint() {
        return new HistoricalPricePoint(date, close, adjustedClose, currency);
    }
}
