import type { RebalanceConfirmResponse, RebalancePreviewResponse } from "../types/rebalance";
  
  const getHeaders = () => {
    const token = localStorage.getItem("token");
  
    if (!token) {
      throw new Error("Ingen token hittades");
    }
  
    return {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    };
  };
  
  export const previewRebalance = async (
    accountId: number,
    allowedInstrumentIds: number[],
  ): Promise<RebalancePreviewResponse> => {
    const response = await fetch(
      `/api/accounts/${accountId}/rebalance/preview`,
      {
        method: "POST",
        headers: getHeaders(),
        body: JSON.stringify({ allowedInstrumentIds }),
      },
    );
  
    if (!response.ok) {
        const errorText = await response.text();
      
        console.error("REBALANCE API ERROR:", {
          status: response.status,
          accountId,
          allowedInstrumentIds,
          errorText,
        });
      
        throw new Error(errorText || "Kunde inte beräkna rebalanseringen");
      }
  
    return response.json();
  };
  
  export const confirmRebalance = async (
    accountId: number,
    proposalToken: string,
  ): Promise<RebalanceConfirmResponse> => {
    const response = await fetch(
      `/api/accounts/${accountId}/rebalance/confirm`,
      {
        method: "POST",
        headers: getHeaders(),
        body: JSON.stringify({ proposalToken }),
      },
    );
  
    if (!response.ok) {
      throw new Error("Kunde inte genomföra rebalanseringen");
    }
  
    return response.json();
  };