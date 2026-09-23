export type Holding = {
  id: number;
  accountId: number;
  accountName: string;
  ticker: string;
  instrumentName: string;
  accountType: string;
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

export type HoldingApiResponse = {
  id: number;
  account_id: number;
  account_name: string;
  account_type: string;
  ticker: string;
  instrument_name: string;
  quantity: number;
  avg_buy_price: number;
  currentPrice: number;
  currency: string;
};
