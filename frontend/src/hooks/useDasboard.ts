import { useState, useEffect } from "react";
import type { DashboardData } from "../types/dashboard";
import { getDashboard } from "../services/dashboardService";

function useDashboard() { 
  const [dashboard, setDashboard] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getDashboard()
      .then(setDashboard)
      .catch(() => {
        setError("Kunde inte hämta portfoliodata");
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  return { dashboard, loading, error };
  }

export default useDashboard;