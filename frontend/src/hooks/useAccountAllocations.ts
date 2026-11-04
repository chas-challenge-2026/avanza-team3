import { useEffect, useState } from "react";
import getAccountAllocation from "../services/accountAllocationService";
import type { AccountAllocation } from "./useAccountAllocation";

export const useAccountAllocations = (accountIds: number[]) => {
  const [allocations, setAllocations] = useState<AccountAllocation[]>([]);
  const [loading, setLoading] = useState(false);
  
  const idsKey = accountIds.join(",");

  useEffect(() => {
    if (!idsKey) return;

    let cancelled = false;

    const fetchAllocations = async () => {
      setLoading(true);

      try {
        const data = await Promise.all(
          idsKey.split(",").map((id) => getAccountAllocation(Number(id)))
        );

        if (!cancelled) {
          setAllocations(data);
        }
      } catch (error) {
        console.error("Kunde inte hämta kontoallokeringar:", error);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchAllocations();

    return () => {
      cancelled = true;
    };
  }, [idsKey]);

  return { allocations, loading };
};