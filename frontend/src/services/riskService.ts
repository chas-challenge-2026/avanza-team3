import type { PortfolioRiskResponse } from "../types/risk";

export const getPortfolioRisk = async (): Promise<PortfolioRiskResponse> => {
  const token = localStorage.getItem("token");

  const response = await fetch("/api/risk/portfolio", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Kunde inte hämta portföljrisk");
  }

  return response.json();
};