export const calculateAccountHealth = (
    categories: {
      actualPct: number;
      targetPct: number | null;
    }[]
  ): number => {
    const totalDeviation = categories.reduce(
      (sum, category) =>
        sum + Math.abs(category.actualPct - (category.targetPct ?? 0)),
      0
    );
  
    return Math.round(Math.max(0, 100 - totalDeviation / 2));
  };