import type { Instrument } from "../types/Instrument";

export const mockApprovedHoldings: Instrument[] = [
  {
    id: 1,
    ticker: "AAPL",
    name: "Apple Inc",
    instrumentType: "STOCK",
    sector: "TECHNOLOGY",
    currency: "USD",
  },
  {
    id: 2,
    ticker: "ERIC-B",
    name: "Ericsson B",
    instrumentType: "STOCK",
    sector: "TECHNOLOGY",
    currency: "SEK",
  },
];
