INSERT INTO instruments (ticker, name, instrument_type, sector, currency)
SELECT demo.ticker, demo.name, demo.instrument_type, demo.sector, demo.currency
FROM (VALUES
          ('FND-SE', 'Demo Sverige Indexfond', 'FUND', 'UNKNOWN', 'SEK'),
          ('FND-WORLD', 'Demo Global Indexfond', 'FUND', 'UNKNOWN', 'SEK'),
          ('GOV-SE', 'Demo Statsobligation SEK', 'BOND', 'UNKNOWN', 'SEK'),
          ('GOLD-ETF', 'Demo Guld ETF (ravaruexponering)', 'ETF', 'MATERIALS', 'SEK')
     ) AS demo(ticker, name, instrument_type, sector, currency)
WHERE NOT EXISTS (SELECT 1 FROM instruments i WHERE UPPER(i.ticker) = demo.ticker);

INSERT INTO instrument_prices (instrument_id, price_date, close_price, adjusted_close, currency, source)
SELECT i.id, DATE '2026-10-30', demo.price, demo.price, 'SEK', 'DEMO_SEED'
FROM (VALUES
          ('FND-SE', 125.00::numeric),
          ('FND-WORLD', 200.00::numeric),
          ('GOV-SE', 100.00::numeric),
          ('GOLD-ETF', 150.00::numeric)
     ) AS demo(ticker, price)
         JOIN instruments i ON UPPER(i.ticker) = demo.ticker
    ON CONFLICT (instrument_id, price_date) DO NOTHING;
