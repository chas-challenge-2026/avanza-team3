export type AccountAllocation = {
    accountId: number;
    totalValueSek: number;
    targetsConfigured: boolean;
    categories: {
      category: string;
      valueSek: number;
      actualPct: number;
      targetPct: number | null;
      differencePctPoints: number | null;
      differenceSek: number | null;
    }[];
  };