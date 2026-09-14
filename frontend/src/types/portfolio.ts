export type AllocationRow = {
  accountType: string;
  actual: number;
  target: number;
  drift: number;
  overThreshold: boolean;
};

export type PortfolioResponse = {
  allocationRows: AllocationRow[];
  totalPortfolioValue: number;
};
