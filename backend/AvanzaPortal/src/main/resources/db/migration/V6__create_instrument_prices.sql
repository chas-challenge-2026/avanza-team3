CREATE TABLE instrument_prices (
    id BIGSERIAL PRIMARY KEY,
    instrument_id INT NOT NULL REFERENCES instruments(id),
    price_date DATE NOT NULL,
    close_price DECIMAL(24,8) NOT NULL CHECK (close_price > 0),
    adjusted_close DECIMAL(24,8) CHECK (adjusted_close > 0),
    currency VARCHAR(3) NOT NULL,
    source VARCHAR(50) NOT NULL,
    CONSTRAINT uk_instrument_price_date UNIQUE (instrument_id, price_date)
);
