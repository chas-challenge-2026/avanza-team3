import type { AccountAllocation } from "./useAccountAllocation";
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

export const usePortfolioHealth = (
  accounts: Account[],
  allocations: AccountAllocation[],
) => {
  const accountHealth: AccountHealth[] = accounts.map((account) => {
    const allocation = allocations.find(
      (item) => item.accountId === account.id,
    );

    return {
      accountId: account.id,
      accountType: account.accountType,
      score: allocation?.targetsConfigured
        ? calculateAccountHealth(allocation.categories)
        : null,
      valueSek: allocation?.totalValueSek ?? 0,
    };
  });

  const validAccounts = accountHealth.filter(
    (account): account is AccountHealth & { score: number } =>
      account.score !== null,
  );

  const portfolioHealth = calculatePortfolioHealth(validAccounts);

  return { portfolioHealth, accountHealth };
};