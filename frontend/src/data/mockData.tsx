type Account = {
  id: number;
  userId: number;
  accountType: string;
  accountName: string;
};

export type Holding = {
  id: number;
  accountId: number;
  ticker: string;
  instrumentName: string;
  quantity: number;
  avgBuyPrice: number;
  currency: string;
};

type TargetAllocation = {
  id: number;
  userId: number;
  accountType: string;
  targetPct: number;
};

type Alert = {
  id: number;
  userId: number;
  alertType: string;
  message: string;
};

const accounts: Account[] = [
  {
    id: 1,
    userId: 1,
    accountType: "ISK",
    accountName: "Anna ISK"
  },
  {
    id: 2,
    userId: 1,
    accountType: "KF",
    accountName: "Anna KF"
  },
  {
    id: 3,
    userId: 1,
    accountType: "Depa",
    accountName: "Anna Depå"
  }
];

export const holdings: Holding[] = [
  {
    id: 1,
    accountId: 1,
    ticker: "ERIC-B",
    instrumentName: "Ericsson B",
    quantity: 500,
    avgBuyPrice: 68.5,
    currency: "SEK"
  },
  {
    id: 2,
    accountId: 1,
    ticker: "VOLV-B",
    instrumentName: "Volvo B",
    quantity: 100,
    avgBuyPrice: 245.0,
    currency: "SEK"
  },
  {
    id: 3,
    accountId: 1,
    ticker: "AAPL",
    instrumentName: "Apple Inc",
    quantity: 50,
    avgBuyPrice: 165.0,
    currency: "USD"
  },
  {
    id: 4,
    accountId: 2,
    ticker: "SWED-A",
    instrumentName: "Swedbank A",
    quantity: 200,
    avgBuyPrice: 185.0,
    currency: "SEK"
  },
  {
    id: 5,
    accountId: 3,
    ticker: "SAND",
    instrumentName: "Sandvik",
    quantity: 300,
    avgBuyPrice: 205.0,
    currency: "SEK"
  }
];
const targetAllocations: TargetAllocation[] = [
  {
    id: 1,
    userId: 1,
    accountType: "ISK",
    targetPct: 60.0
  },
  {
    id: 2,
    userId: 1,
    accountType: "KF",
    targetPct: 25.0
  },
  {
    id: 3,
    userId: 1,
    accountType: "Depa",
    targetPct: 15.0
  }
];

const alerts: Alert[] = [
  {
    id: 1,
    userId: 1,
    alertType: "DRIFT",
    message: "ISK-allokering avviker 8% från mål (60%). Överväg ombalansering."
  },
  {
    id: 2,
    userId: 1,
    alertType: "DRIFT",
    message: "KF-allokering avviker 6% från mål (25%)."
  }
];
