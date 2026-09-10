import type { DashboardResponse } from "../types/dashboard";

export async function getDashboard(token: string): Promise<DashboardResponse> {
  const url = "http://localhost:8082/api/portfolio";

  const response = await fetch(url, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Kunde inte hämta kontodata");
  }

  return response.json();
}
