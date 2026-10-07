import { useEffect, useState } from "react";
import { getAccounts } from "../services/accountService";
import type { Account } from "../types/account";

export const useAccounts = () => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadAccounts = async () => {
      try {
        const data = await getAccounts();
        setAccounts(data);
      } catch {
        setError("Kunde inte hämta konton");
      } finally {
        setLoading(false);
      }
    };

    loadAccounts();
  }, []);

  return {
    accounts,
    loading,
    error,
  };
};