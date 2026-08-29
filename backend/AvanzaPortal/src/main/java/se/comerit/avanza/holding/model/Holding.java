package se.comerit.avanza.holding.model;

import jakarta.persistence.*;
import se.comerit.avanza.account.model.Account;


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

    private String ticker;

    @Column(name = "instrument_name")
    private String instrumentName;


    //OBS!!! snacka med gruppen om BigDecimal vid framtida möte
    private Double quantity;

    @Column(name = "avg_buy_price")
    private Double avgBuyPrice;

    private String currency;

    protected Holding() {}

    public Holding(Integer accountId, String ticker, String instrumentName, Double quantity, Double avgBuyPrice, String currency) {
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

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getInstrumentName() {
        return instrumentName;
    }

    public void setInstrumentName(String instrumentName) {
        this.instrumentName = instrumentName;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getAvgBuyPrice() {
        return avgBuyPrice;
    }

    public void setAvgBuyPrice(Double avgBuyPrice) {
        this.avgBuyPrice = avgBuyPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Account getAccount() {
        return account;
    }
}
