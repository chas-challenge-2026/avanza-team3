import { useEffect, useState } from "react";
import { getPortfolioRisk } from "../services/riskService";
import type { PortfolioRiskResponse } from "../types/risk";

export const usePortfolioRisk = () => {
  const [riskData, setRiskData] = useState<PortfolioRiskResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchRisk = async () => {
      try {
        const data = await getPortfolioRisk();
        setRiskData(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Något gick fel");
      } finally {
        setLoading(false);
      }
    };

    fetchRisk();
  }, []);

  return { riskData, loading, error };
};