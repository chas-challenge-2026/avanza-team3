package se.comerit.avanza.account.model;

import jakarta.persistence.*;



@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "account_type", length = 10)
    private String accountType;

    @Column(name = "account_name", length = 100)
    private String accountName;

    @Column(name = "currency", length = 3)
    private String currency = "SEK";


    @Column(name = "cash_balance_sek", nullable = false, precision = 16, scale = 2)
    private java.math.BigDecimal cashBalanceSek = java.math.BigDecimal.ZERO;

    @Column(name = "rebalance_version", nullable = false)
    private int rebalanceVersion = 0;

    public java.math.BigDecimal getCashBalanceSek() { return cashBalanceSek; }
    public void setCashBalanceSek(java.math.BigDecimal cashBalanceSek) { this.cashBalanceSek = cashBalanceSek; }
    public int getRebalanceVersion() { return rebalanceVersion; }
    public void setRebalanceVersion(int rebalanceVersion) { this.rebalanceVersion = rebalanceVersion; }

    protected Account() {
    }

    public Account(Integer userId, String accountType, String accountName, String currency) {
        this.userId = userId;
        this.accountType = accountType;
        this.accountName = accountName;
        this.currency = currency;
    }

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
