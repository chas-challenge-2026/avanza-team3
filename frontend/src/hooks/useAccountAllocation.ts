import { useEffect, useState } from "react";
import getAccountAllocation from "../services/accountAllocationService";

type AllocationCategory = {
  category: string;
  valueSek: number;
  actualPct: number;
  targetPct: number | null;
  differencePctPoints: number | null;
  differenceSek: number | null;
};

export type AccountAllocation = {
  accountId: number;
  totalValueSek: number;
  targetsConfigured: boolean;
  categories: AllocationCategory[];
};

export const useAccountAllocation = (accountId: number | null) => {
  const [allocation, setAllocation] = useState<AccountAllocation | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (accountId === null) {
      return;
    }

    let cancelled = false;

    const fetchAllocation = async () => {
      setLoading(true);
      setError(null);
      setAllocation(null);

      try {
        const data: AccountAllocation =
          await getAccountAllocation(accountId);

        if (!cancelled) {
          setAllocation(data);
        }
      } catch {
        if (!cancelled) {
          setError("Kunde inte hämta kontots allokering");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    };

    fetchAllocation();

    return () => {
      cancelled = true;
    };
  }, [accountId]);

  return { allocation, loading, error };
};