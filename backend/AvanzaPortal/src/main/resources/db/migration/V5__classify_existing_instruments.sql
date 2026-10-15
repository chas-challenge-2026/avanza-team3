UPDATE instruments
SET instrument_type = 'STOCK'
WHERE instrument_type = 'UNKNOWN'
  AND UPPER(ticker) IN (
      'AAPL', 'ERIC-B', 'VOLV-B', 'SWED-A', 'SAND'
  );

UPDATE instruments
SET sector = CASE UPPER(ticker)
    WHEN 'AAPL' THEN 'TECHNOLOGY'
    WHEN 'ERIC-B' THEN 'TECHNOLOGY'
    WHEN 'VOLV-B' THEN 'INDUSTRIALS'
    WHEN 'SWED-A' THEN 'FINANCIALS'
    WHEN 'SAND' THEN 'INDUSTRIALS'
    ELSE sector
END
WHERE sector = 'UNKNOWN'
  AND UPPER(ticker) IN (
      'AAPL', 'ERIC-B', 'VOLV-B', 'SWED-A', 'SAND'
  );
