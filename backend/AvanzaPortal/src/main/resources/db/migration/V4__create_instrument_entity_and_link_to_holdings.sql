CREATE TABLE instruments (
                             id SERIAL PRIMARY KEY,
                             name VARCHAR(100) NOT NULL,
                             sector VARCHAR(100) NOT NULL,
                             instrument_type VARCHAR(20) NOT NULL,
                             currency VARCHAR(3) NOT NULL,
                             ticker VARCHAR(20) NOT NULL
);

INSERT INTO instruments (
    ticker,
    name,
    instrument_type,
    sector,
    currency
)
SELECT DISTINCT ON (UPPER(ticker))
    ticker,
    instrument_name,
    'UNKNOWN',
    'UNKNOWN',
    currency
FROM holdings
ORDER BY UPPER(ticker), id;

CREATE UNIQUE INDEX uq_instruments_ticker_upper
    ON instruments (UPPER(ticker));

ALTER TABLE holdings
    ADD COLUMN instrument_id INT;

UPDATE holdings h
SET instrument_id = i.id
    FROM instruments i
WHERE UPPER(h.ticker) = UPPER(i.ticker);

ALTER TABLE holdings
    ADD CONSTRAINT fk_holdings_instrument
        FOREIGN KEY (instrument_id)
            REFERENCES instruments(id);

ALTER TABLE holdings
    ALTER COLUMN instrument_id SET NOT NULL;