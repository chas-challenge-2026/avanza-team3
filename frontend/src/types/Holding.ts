export type Holding = {
    id: number;
    accountId: number;
    ticker: string;
    instrumentName: string;
    quantity: number;
    avgBuyPrice: number;
    currentPrice: number;
    currency: string;
  };
  
  export interface HoldingRequest {
    accountId: number;
    ticker: string;
    instrumentName: string;
    quantity: number;
    avgBuyPrice: number;
    currency: string;
  }

