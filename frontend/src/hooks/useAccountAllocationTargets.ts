import { useEffect, useState } from "react";
import type { AccountAllocationTarget } from "../types/accountAllocationTargets";
import { getAccountAllocationTargets } from "../services/accountAllocationTargetsService";

export const useAccountAllocationTargets = (accountIds: number[]) => {
  const [targetsByAccount, setTargetsByAccount] = useState<
    Record<number, AccountAllocationTarget[]>
  >({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const accountIdsKey = accountIds.join(",");

  useEffect(() => {
    const loadTargets = async () => {
      if (!accountIdsKey) {
        setTargetsByAccount({});
        setLoading(false);
        return;
      }
      setLoading(true);
      setError(null);

      try {
        const ids = accountIdsKey.split(",").map(Number);
        const responses = await Promise.all(
          ids.map((id) => getAccountAllocationTargets(id))
        );
        const results: Record<number, AccountAllocationTarget[]> = {};
        responses.forEach((response) => {
          results[response.accountId] = response.targets;
        });
        setTargetsByAccount(results);
      } catch (error) {
        setError(
          error instanceof Error ? error.message : "kunde inte hämta konto mål"
        );
      } finally {
        setLoading(false);
      }
    };
    void loadTargets();
  }, [accountIdsKey]);
  return { targetsByAccount, loading, error };
};
