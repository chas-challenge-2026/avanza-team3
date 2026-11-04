export type HoldingSummary = {
    id: number;
    account_id: number;
    instrument_id: number;
    ticker: string;
    instrument_name: string;
    instrument_type: string;
    sector: string;
    quantity: number;
    avg_buy_price: number;
    currency: string;
    account_type: string;
    account_name: string;
    currentPrice: number;
    marketValue: number;
    pnl: number;
    pnlPct: number;
  };
  
  export const getHoldingsSummary = async (): Promise<HoldingSummary[]> => {
    const response = await fetch("/api/holdings/summary", {
      headers: {
        Authorization: `Bearer ${localStorage.getItem("token")}`,
      },
    });
  
    if (!response.ok) {
      throw new Error("Kunde inte hämta innehavssammanställning");
    }
  
    return response.json();
  };