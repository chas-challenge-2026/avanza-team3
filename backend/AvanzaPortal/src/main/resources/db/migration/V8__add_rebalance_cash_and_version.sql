ALTER TABLE accounts
    ADD COLUMN cash_balance_sek NUMERIC(16,2) NOT NULL DEFAULT 0.00
        CHECK (cash_balance_sek >= 0),
    ADD COLUMN rebalance_version INTEGER NOT NULL DEFAULT 0
        CHECK (rebalance_version >= 0);
