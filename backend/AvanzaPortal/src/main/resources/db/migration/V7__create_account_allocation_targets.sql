-- Account-level asset allocation targets; existing target_allocations remains unchanged.
CREATE TABLE account_allocation_targets (
    id SERIAL PRIMARY KEY,
    account_id INT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    asset_category VARCHAR(20) NOT NULL,
    target_pct DECIMAL(5,2) NOT NULL,
    CONSTRAINT chk_account_target_percentage CHECK (target_pct >= 0 AND target_pct <= 100),
    CONSTRAINT chk_account_target_category CHECK (asset_category IN ('STOCK', 'FUND', 'BOND', 'OTHER')),
    CONSTRAINT uq_account_target_category UNIQUE (account_id, asset_category)
);

