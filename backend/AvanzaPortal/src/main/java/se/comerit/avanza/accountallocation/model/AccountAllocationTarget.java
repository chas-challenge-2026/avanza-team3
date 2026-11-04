package se.comerit.avanza.accountallocation.model;

import jakarta.persistence.*;
import se.comerit.avanza.account.model.Account;
import java.math.BigDecimal;

@Entity
@Table(name = "account_allocation_targets",
        uniqueConstraints = @UniqueConstraint(name = "uq_account_target_category",
                columnNames = {"account_id", "asset_category"}))
public class AccountAllocationTarget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_category", nullable = false, length = 20)
    private AssetCategory assetCategory;

    @Column(name = "target_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal targetPct;

    protected AccountAllocationTarget() { }

    public AccountAllocationTarget(Account account, AssetCategory assetCategory, BigDecimal targetPct) {
        this.account = account;
        this.assetCategory = assetCategory;
        this.targetPct = targetPct;
    }

    public Integer getId() { return id; }
    public Account getAccount() { return account; }
    public AssetCategory getAssetCategory() { return assetCategory; }
    public BigDecimal getTargetPct() { return targetPct; }
    public void setTargetPct(BigDecimal targetPct) { this.targetPct = targetPct; }
}
