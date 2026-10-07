import type { Account } from "../types/account";

export const getAccounts = async (): Promise<Account[]> => {
  const token = localStorage.getItem("token");

  if (!token) {
    throw new Error("Ingen token hittades");
  }

  const response = await fetch("/api/accounts", {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error(`Kunde inte hämta konton: ${response.status}`);
  }

  const data = await response.json();

  return data.content;
};