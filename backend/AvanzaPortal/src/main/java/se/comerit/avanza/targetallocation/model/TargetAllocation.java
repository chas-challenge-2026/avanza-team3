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

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "target_pct", nullable = false)
    private Double targetPct;

    protected TargetAllocation() {
    }

    public TargetAllocation(Integer userId, String accountType, Double targetPct) {
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

    public Double getTargetPct() {
        return targetPct;
    }

    public void setTargetPct(Double targetPct) {
        this.targetPct = targetPct;
    }
}
