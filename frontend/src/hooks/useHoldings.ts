import { useEffect, useState } from "react";
import {
  getHoldings,
  deleteHolding,
  createHolding,
  updateHolding,
} from "../services/holdingService";
import type { Holding, HoldingRequest, HoldingPatchRequest } from "../types/Holding";

export const useHoldings = () => {
    const [holdings, setHoldings] = useState<Holding[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
  
    useEffect(() => {
      const loadHoldings = async () => {
        try {
          const data = await getHoldings();
          setHoldings(data);
        } catch {
          setError("Kunde inte hämta innehav");
        } finally {
          setLoading(false);
        }
      };
  
      loadHoldings();
    }, []);
  
    const refreshHoldings = async () => {
      const data = await getHoldings();
      setHoldings(data);
    };
  
    const addHolding = async (holding: HoldingRequest) => {
      await createHolding(holding);
      await refreshHoldings();
    };
  
    const removeHolding = async (id: number) => {
      await deleteHolding(id);
  
      setHoldings((current) =>
        current.filter((holding) => holding.id !== id)
      );
    };

    const editHolding = async (
      id: number,
      changes: HoldingPatchRequest
    ) => {
      await updateHolding(id, changes);
      await refreshHoldings();
    };
  
    return {
      holdings,
      loading,
      error,
      addHolding,
      removeHolding,
      editHolding
    };
  };