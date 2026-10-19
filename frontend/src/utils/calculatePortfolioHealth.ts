export function calculatePortfolioHealth(
    riskScore: number,
    spread: number
  ): number {
    return Math.round(
      Math.max(
        0,
        Math.min(100, (100 - riskScore) * 0.7 + spread * 0.3)
      )
    );
  }