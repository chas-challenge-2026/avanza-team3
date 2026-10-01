package se.comerit.avanza.holding.model;

import jakarta.persistence.*;
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.instrument.model.Instrument;

import java.math.BigDecimal;


@Entity
@Table(name = "holdings")
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "account_id")
    private Integer accountId;

    @ManyToOne
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id")
    private Instrument instrument;

    @Column(length = 20)
    private String ticker;

    @Column(name = "instrument_name", length = 100)
    private String instrumentName;


    //OBS!!! snacka med gruppen om BigDecimal vid framtida möte
    private BigDecimal quantity;

    @Column(name = "avg_buy_price")
    private BigDecimal avgBuyPrice;

    @Column(length = 3)
    private String currency;

    protected Holding() {}

    public Holding(Integer accountId, Instrument instrument, BigDecimal quantity, BigDecimal avgBuyPrice) {
        this.accountId = accountId;
        this.quantity = quantity;
        this.avgBuyPrice = avgBuyPrice;
        setInstrument(instrument);
    }

    @Deprecated
    public Holding(Integer accountId, String ticker, String instrumentName, BigDecimal quantity, BigDecimal avgBuyPrice, String currency) {
        this.accountId = accountId;
        this.ticker = ticker;
        this.instrumentName = instrumentName;
        this.quantity = quantity;
        this.avgBuyPrice = avgBuyPrice;
        this.currency = currency;
    }

    public Integer getId() {
        return id;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
        if (instrument != null) {
            this.ticker = instrument.getTicker();
            this.instrumentName = instrument.getName();
            this.currency = instrument.getCurrency();
        }
    }

    public String getTicker() {
        return ticker;
    }

    @Deprecated
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    @Deprecated
    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getAvgBuyPrice() {
        return avgBuyPrice;
    }

    public void setAvgBuyPrice(BigDecimal avgBuyPrice) {
        this.avgBuyPrice = avgBuyPrice;
    }

    public String getCurrency() {
        return currency;
    }

    @Deprecated
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Account getAccount() {
        return account;
    }
}
