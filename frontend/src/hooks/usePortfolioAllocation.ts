import { useEffect, useState } from "react";
import { getDashboard } from "../services/dashboardService";
import type { AllocationRow } from "../types/dashboard";

const usePortfolioAllocations = () => {
  const [rows, setRows] = useState<AllocationRow[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getDashboard()
      .then((portfolio) => {
        setRows(portfolio.allocationRows);
      })
      .catch(() => {
        setError("Kunde inte hämta fördelningen");
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);
  return { rows, error, loading };
};
export default usePortfolioAllocations;
