import type { DashboardData } from "../types/dashboard";

export async function getDashboard(): Promise<DashboardData> {
  const response = await fetch("/api/portfolio", {
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error("Kunde inte hämta portfoliodata");
  }

  return response.json();
}