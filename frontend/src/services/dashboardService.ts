import type { DashboardData } from "../types/dashboard";

export async function getDashboard(): Promise<DashboardData> {
  const token = localStorage.getItem("token");

  const response = await fetch("/api/portfolio", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Kunde inte hämta portfoliodata");
  }

  return response.json();
}
