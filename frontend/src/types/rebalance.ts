export type RebalanceTrade = {
    side: "BUY" | "SELL";
    instrumentId: number;
    ticker: string;
    category: "STOCK" | "FUND" | "BOND" | "OTHER";
    quantity: number;
    unitPriceSek: number;
    amountSek: number;
  };
  
  export type RebalanceProposedHolding = {
    instrumentId: number;
    ticker: string;
    instrumentName: string;
    category: "STOCK" | "FUND" | "BOND" | "OTHER";
    currentQuantity: number;
    proposedQuantity: number;
    unitPriceSek: number;
    currentValueSek: number;
    proposedValueSek: number;
  };
  
  export type RebalanceAllocationRow = {
    category: "STOCK" | "FUND" | "BOND" | "OTHER";
    valueSek: number;
    actualPct: number;
    targetPct: number;
    differencePctPoints: number;
  };

  export type RebalancePreviewResponse = {
    accountId: number;
    proposalToken: string | null;
    totalValueSek: number;
    cashBeforeSek: number;
    cashAfterSek: number;
    executable: boolean;
    before: RebalanceAllocationRow[];
    after: RebalanceAllocationRow[];
    trades: RebalanceTrade[];
    proposedHoldings: RebalanceProposedHolding[];
    warnings: string[];
  };

  export type RebalanceConfirmResponse = {
    accountId: number;
    status: string;
    cashAfterSek: number;
    trades: RebalanceTrade[];
  };