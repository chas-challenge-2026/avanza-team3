export type MockPortfolioHolding = {
  ticker: string;
  instrumentName: string;
  instrumentType: "Aktie" | "Fond";
  accountType: "ISK" | "KF" | "Depa";
  accountName: string;
  quantity: number;
  currentPrice: number;
  marketValueSek: number;
};

const holdings: MockPortfolioHolding[] = [
  {
    ticker: "INVE-B",
    instrumentName: "Investor B",
    instrumentType: "Aktie",
    accountType: "ISK",
    accountName: "Anna ISK",
    quantity: 20,
    currentPrice: 320,
    marketValueSek: 6400
  },
  {
    ticker: "AVANZA-GLOBAL",
    instrumentName: "Avanza Global",
    instrumentType: "Fond",
    accountType: "ISK",
    accountName: "Anna ISK",
    quantity: 50,
    currentPrice: 156,
    marketValueSek: 7800
  },
  {
    ticker: "VOLV-B",
    instrumentName: "Volvo B",
    instrumentType: "Aktie",
    accountType: "KF",
    accountName: "Anna KF",
    quantity: 15,
    currentPrice: 290,
    marketValueSek: 4350
  },
  {
    ticker: "LF-GLOBAL",
    instrumentName: "Länsförsäkringar Global Index",
    instrumentType: "Fond",
    accountType: "KF",
    accountName: "Anna KF",
    quantity: 25,
    currentPrice: 240,
    marketValueSek: 6000
  },
  {
    ticker: "ATCO-A",
    instrumentName: "Atlas Copco A",
    instrumentType: "Aktie",
    accountType: "Depa",
    accountName: "Anna Depå",
    quantity: 8,
    currentPrice: 200,
    marketValueSek: 1600
  },
  {
    ticker: "SPILTAN-INV",
    instrumentName: "Spiltan Aktiefond Investmentbolag",
    instrumentType: "Fond",
    accountType: "Depa",
    accountName: "Anna Depå",
    quantity: 12,
    currentPrice: 350,
    marketValueSek: 4200
  }
];

const accountTypes = ["ISK", "KF", "Depa"] as const;

const instrumentTypes = [
  { type: "Aktie", label: "Aktie", color: "#16a34a" },
  { type: "Fond", label: "Fond", color: "#1976d2" }
] as const;

const sumMarketValue = (items: MockPortfolioHolding[]) =>
  items.reduce((total, holding) => total + holding.marketValueSek, 0);

const accountTypeInstrumentAllocations = accountTypes.map((accountType) => {
  const accountHoldings = holdings.filter(
    (holding) => holding.accountType === accountType
  );

  return {
    accountType,
    instrumentAllocations: instrumentTypes.map(({ type, label, color }) => {
      const value = sumMarketValue(
        accountHoldings.filter((holding) => holding.instrumentType === type)
      );

      return {
        label,
        value,
        color
      };
    })
  };
});

export const mockPortfolioData = {
  holdings,
  instrumentTypeAllocations: instrumentTypes.map(({ type, label, color }) => ({
    label,
    value: sumMarketValue(
      holdings.filter((holding) => holding.instrumentType === type)
    ),
    color
  })),
  accountTypeInstrumentAllocations
};
