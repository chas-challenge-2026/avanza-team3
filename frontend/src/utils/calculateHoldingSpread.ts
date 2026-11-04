type Holding = {
    ticker: string;
    valueSek: number;
  };
  
  // Herfindahl-Hirschman Index (HHI).
  export const calculateHoldingSpread = (
    holdings: Holding[]
  ): number => {
    const valuesByTicker = new Map<string, number>();
  
    for (const holding of holdings) {
      valuesByTicker.set(
        holding.ticker,
        (valuesByTicker.get(holding.ticker) ?? 0) +
          holding.valueSek
      );
    }
  
    const values = [...valuesByTicker.values()];
  
    const totalValue = values.reduce(
      (sum, value) => sum + value,
      0
    );
  
    if (totalValue <= 0) return 0;
  
    const hhi = values.reduce((sum, value) => {
      const weight = value / totalValue;
      return sum + weight ** 2;
    }, 0);
  
    return Math.round(100 * (1 - hhi));
  };