import { useEffect, useState } from "react";
import { getPortfolio } from "../services/portfolioService";
import type { AllocationRow } from "../types/portfolio";

const usePortfolioAllocations = () => {
  const [rows, setRows] = useState<AllocationRow[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getPortfolio()
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
