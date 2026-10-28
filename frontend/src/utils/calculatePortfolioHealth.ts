export function calculatePortfolioHealth(
  riskScore: number,
  spread: number
): number {
  const riskBalance = 100 - riskScore;

  return Math.round(
    Math.max(
      0,
      Math.min(100, riskBalance * 0.5 + spread * 0.5)
    )
  );
}