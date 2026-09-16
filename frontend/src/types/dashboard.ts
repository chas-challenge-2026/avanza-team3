export type DashboardData = {
  accounts: AccountSummary[];
  holdings: DashboardHoldingPage;
  allocationRows: AllocationRow[];
  totalPortfolioValue: number;
  recentAlerts: RecentAlert[];
  anyDrift: boolean;
  usdToSek: number;
};

export type AccountSummary = {
  id: number;
  userId: number;
  accountType: string;
  accountName: string;
  currency: string;
  totalValue: number;
};

export type DashboardHolding = {
  id: number;
  accountId: number;
  ticker: string;
  instrumentName: string;
  quantity: number;
  avgBuyPrice: number;
  currency: string;
  accountType: string;
  accountName: string;
  currentPrice: number;
  valueSek: number;
  unrealizedReturn: number;
  unrealizedReturnPct: number;
  sharpe: number;
  fxInfo: string;
};

export type DashboardHoldingPage = {
  content: DashboardHolding[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export type AllocationRow = {
  accountType: string;
  actual: number;
  target: number;
  drift: number;
  overThreshold: boolean;
};

export type RecentAlert = {
  id: number;
  alertType: string;
  message: string;
  createdAt: string;
};