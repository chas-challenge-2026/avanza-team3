export type RiskMetrics = {
    volatility: number;
    sharpeRatio: number;
    maxDrawdown: number;
    riskScore: number;
  };
  
  export type RiskHistoryPoint = {
    date: string;
    portfolioValue: number;
    metrics: RiskMetrics;
  };
  
  export type PortfolioRiskResponse = {
    baseCurrency: string;
    historySource: string;
    current: RiskMetrics;
    history: RiskHistoryPoint[];
  };