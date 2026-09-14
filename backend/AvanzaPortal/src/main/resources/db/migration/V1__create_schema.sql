CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(100)
);

CREATE TABLE accounts (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    account_type VARCHAR(10),
    account_name VARCHAR(100),
    currency VARCHAR(3) DEFAULT 'SEK'
);

CREATE TABLE holdings (
    id SERIAL PRIMARY KEY,
    account_id INT REFERENCES accounts(id),
    ticker VARCHAR(20),
    instrument_name VARCHAR(100),
    quantity DECIMAL(12,4),
    avg_buy_price DECIMAL(12,2),
    currency VARCHAR(3) DEFAULT 'SEK'
);

CREATE TABLE target_allocations (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    account_type VARCHAR(10),
    target_pct DECIMAL(5,2)
);

CREATE TABLE alerts (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    alert_type VARCHAR(50),
    message TEXT,
    dismissed BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT NOW()
);