import { useEffect, useState } from "react";
import getAccountAllocation from "../services/accountAllocationService";
import { calculateAccountHealth } from "../utils/calculateAccountHealth";
import { calculatePortfolioHealth } from "../utils/calculatePortfolioHealth";

type Account = {
  id: number;
  accountType: string;
};

type AccountHealth = {
  accountId: number;
  accountType: string;
  score: number | null;
  valueSek: number;
};

type HealthState = {
  accountHealth: AccountHealth[];
  loading: boolean;
  error: string | null;
  accounts: Account[];
};

export const usePortfolioHealth = (accounts: Account[]) => {
  const [state, setState] = useState<HealthState>({
    accountHealth: [],
    loading: false,
    error: null,
    accounts: [],
  });

  useEffect(() => {
    if (accounts.length === 0) return;

    let cancelled = false;

    const fetchHealth = async () => {
      try {
        const results: AccountHealth[] = await Promise.all(
          accounts.map(async (account) => {
            const allocation = await getAccountAllocation(account.id);

            return {
              accountId: account.id,
              accountType: account.accountType,
              score: allocation.targetsConfigured
                ? calculateAccountHealth(allocation.categories)
                : null,
              valueSek: allocation.totalValueSek,
            };
          }),
        );

        if (!cancelled) {
          setState({
            accountHealth: results,
            loading: false,
            error: null,
            accounts,
          });
        }
      } catch (error) {
        console.error("Kunde inte hämta portföljhälsa:", error);

        if (!cancelled) {
          setState({
            accountHealth: [],
            loading: false,
            error: "Kunde inte hämta portföljhälsa",
            accounts,
          });
        }
      }
    };

    fetchHealth();

    return () => {
      cancelled = true;
    };
  }, [accounts]);

  // Visa inte gamla resultat när kontona ändras.
  const isCurrent = state.accounts === accounts;
  const accountHealth = isCurrent ? state.accountHealth : [];
  const loading = accounts.length > 0 && !isCurrent;
  const error = isCurrent ? state.error : null;

  const validAccounts = accountHealth.filter(
    (account): account is AccountHealth & { score: number } =>
      account.score !== null,
  );

  const portfolioHealth = calculatePortfolioHealth(validAccounts);

  return { portfolioHealth, accountHealth, loading, error };
};