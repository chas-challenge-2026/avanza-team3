import { useState, useEffect } from "react";
import type { DashboardData } from "../types/dashboard";

function useDashboard() {
    const [dashboard, setDashboard] =
      useState<DashboardData | null>(null);
  
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
  
    useEffect(() => {
      const fetchDashboard = async () => {
        try {
          const data = await getDashboard();
          setDashboard(data);
        } catch {
          setError("Kunde inte hämta dashboard");
        } finally {
          setLoading(false);
        }
      };
  
      fetchDashboard();
    }, []);
  
    return { dashboard, loading, error };
  }