import type {
  AccountAllocationTargetsResponse,
  ReplaceAccountTargetsRequest
} from "../types/accountAllocationTargets";

export const getAccountAllocationTargets = async (
  accountId: number
): Promise<AccountAllocationTargetsResponse> => {
  const token = localStorage.getItem("token");

  if (!token) {
    throw new Error("Ingen token hittades");
  }

  const response = await fetch(
    `/api/accounts/${accountId}/allocation-targets`,
    {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`
      }
    }
  );

  if (!response.ok) {
    throw new Error(`Kunde inte hämta kontomål ${response.status}`);
  }
  return response.json();
};

export const replaceAccountAllocationTargets = async (
  accountId: number,
  targets: ReplaceAccountTargetsRequest
): Promise<AccountAllocationTargetsResponse> => {
  const token = localStorage.getItem("token");

  if (!token) {
    throw new Error("Ingen token hittades");
  }

  const response = await fetch(
    `/api/accounts/${accountId}/allocation-targets`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(targets)
    }
  );
  if (!response.ok) {
    throw new Error(`Kunde inte spara kontomål: ${response.status}`);
  }
  return response.json();
};
