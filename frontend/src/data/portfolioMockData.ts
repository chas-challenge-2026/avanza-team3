export type MockPortfolioHolding = {
  ticker: string;
  instrumentName: string;
  instrumentType: "Aktie" | "Fond";
  assetClass: "Aktier" | "Räntor" | "Övrigt";
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
    assetClass: "Aktier",
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
    assetClass: "Aktier",
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
    assetClass: "Aktier",
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
    assetClass: "Aktier",
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
    assetClass: "Aktier",
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
    assetClass: "Aktier",
    accountType: "Depa",
    accountName: "Anna Depå",
    quantity: 12,
    currentPrice: 350,
    marketValueSek: 4200
  },
  {
    ticker: "AVANZA-RANTA-KORT",
    instrumentName: "Avanza Ränta Kort",
    instrumentType: "Fond",
    assetClass: "Räntor",
    accountType: "ISK",
    accountName: "Anna ISK",
    quantity: 100,
    currentPrice: 50,
    marketValueSek: 5000
  },
  {
    ticker: "XACT-RAVAROR",
    instrumentName: "XACT Råvaror",
    instrumentType: "Fond",
    assetClass: "Övrigt",
    accountType: "KF",
    accountName: "Anna KF",
    quantity: 20,
    currentPrice: 100,
    marketValueSek: 2000
  }
];

const accountTypes = ["ISK", "KF", "Depa"] as const;

const assetClasses = [
  { type: "Aktier", label: "Aktier", color: "#16a34a" },
  { type: "Räntor", label: "Räntor", color: "#1976d2" },
  { type: "Övrigt", label: "Övrigt", color: "#f59e0b" }
] as const;

const sumMarketValue = (items: MockPortfolioHolding[]) =>
  items.reduce((total, holding) => total + holding.marketValueSek, 0);

const accountTypeAssetClassAllocations = accountTypes.map((accountType) => {
  const accountHoldings = holdings.filter(
    (holding) => holding.accountType === accountType
  );

  return {
    accountType,
    assetClassAllocations: assetClasses.map(({ type, label, color }) => {
      const value = sumMarketValue(
        accountHoldings.filter((holding) => holding.assetClass === type)
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
  assetClassAllocations: assetClasses.map(({ type, label, color }) => ({
    label,
    value: sumMarketValue(
      holdings.filter((holding) => holding.assetClass === type)
    ),
    color
  })),
  accountTypeAssetClassAllocations
};
