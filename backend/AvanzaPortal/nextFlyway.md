UPDATE instruments
SET instrument_type = 'STOCK'
WHERE instrument_type = 'UNKNOWN'
AND UPPER(ticker) IN (
'AAPL', 'ERIC-B', 'VOLV-B', 'SWED-A', 'SAND');

UPDATE instruments
SET sector = CASE UPPER(ticker)
when 'AAPL' THEN 'Technology'
WHEN 'ERIC-B' THEN 'TECHNOLOGY'
WHEN 'VOLV-B' THEN 'INDUSTRIALS'
WHEN 'SWED-A' THEN 'FINANCIALS'
WHEN 'SAND' THEN 'INDUSTRIALS'
ELSE sector END
WHERE sector IS 'UNKNOWN'
AND UPPER(ticker) IN (
'AAPL', 'ERIC-B', 'VOLV-B', 'SWED-A', 'SAND');


detta kommer lägga till sector och instrument_type på redan existerande aktierna. Men iom det kommer andra flywayfiler vill jag inte köra än. Copy/paste på det ovan in i nästa flyway borde fungera(beroende på vad andra skapat som kan krocka)