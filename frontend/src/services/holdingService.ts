import type { Holding, HoldingRequest } from "../types/Holding";

export const getHoldings = async (): Promise<Holding[]> => {
    const token = localStorage.getItem("token");
  
    if (!token) {
      throw new Error("Ingen token hittades");
    }
  
    const response = await fetch("/api/holdings", {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  
    if (!response.ok) {
      throw new Error(`Kunde inte hämta holdings: ${response.status}`);
    }
  
    const data = await response.json();
    return data.content;
  };

export const createHolding = async (
    holding: HoldingRequest
  ): Promise<void> => {
    const token = localStorage.getItem("token");
  
    if (!token) {
      throw new Error("Ingen token hittades");
    }
  
    const response = await fetch("/api/holdings", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify(holding),
    });
  
    if (!response.ok) {
      throw new Error("Could not create holding");
    }
  };

export const deleteHolding = async (
    holdingId: number
):Promise<void> => {
    const token = localStorage.getItem('token');

    if (!token) {
        throw new Error('No token found')
    }

    const response = await fetch(`/api/holdings/${holdingId}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${token}`,
      },    
    });
    if (!response.ok) {
        throw new Error("Delete response failed");
      }
};
