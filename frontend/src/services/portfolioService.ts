import type { PortfolioResponse } from "../types/portfolio";

export async function getPortfolio(): Promise<PortfolioResponse> {
  const token = localStorage.getItem("token");

  const response = await fetch("/api/portfolio", {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
  if (!response.ok) {
    throw new Error("kunde inte hämta portfoliodata");
  }
  const data = await response.json();
  console.log(data);
  return data;
}
