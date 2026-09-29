package se.comerit.avanza.instrument.model;

import jakarta.persistence.*;

@Entity
@Table(name = "instruments")
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    //Osäker, kanske bättre som enum
    @Column(length = 100)
    private String sector;

    private String instrumentType;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 10, unique = true)
    private String ticker;

    protected Instrument() {}

    public Instrument(String name, String sector, String instrumentType, String currency, String ticker) {
        this.name = name;
        this.sector = sector;
        this.instrumentType = instrumentType;
        this.currency = currency;
        this.ticker = ticker;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public void setInstrumentType(String instrumentType) {
        this.instrumentType = instrumentType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }
}
