package se.comerit.avanza.targetallocation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "target_allocations")
public class TargetAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "account_type", length = 10)
    private String accountType;

    @Column(name = "target_pct", precision = 5, scale = 2)
    private BigDecimal targetPct;

    protected TargetAllocation() {
    }

    public TargetAllocation(Integer userId, String accountType, BigDecimal targetPct) {
        this.userId = userId;
        this.accountType = accountType;
        this.targetPct = targetPct;
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

    public BigDecimal getTargetPct() {
        return targetPct;
    }

    public void setTargetPct(BigDecimal targetPct) {
        this.targetPct = targetPct;
    }
}
