type AccountHealth = {
  score: number;
  valueSek: number;
};

export const calculatePortfolioHealth = (
  accounts: AccountHealth[]
): number | null => {
  const totalValue = accounts.reduce(
    (sum, account) => sum + account.valueSek,
    0
  );

  if (totalValue <= 0) return null;

  const weightedScore = accounts.reduce(
    (sum, account) => sum + account.score * account.valueSek,
    0
  );

  return Math.round(weightedScore / totalValue);
};