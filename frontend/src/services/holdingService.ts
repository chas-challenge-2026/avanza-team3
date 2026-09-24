import type {
  Holding,
  HoldingRequest,
  HoldingApiResponse,
} from "../types/Holding";

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
  return data.content.map((holding: HoldingApiResponse) => ({
    id: holding.id,
    accountId: holding.account_id,
    ticker: holding.ticker,
    instrumentName: holding.instrument_name,
    quantity: holding.quantity,
    avgBuyPrice: holding.avg_buy_price,
    currentPrice: holding.currentPrice,
    currency: holding.currency,
    account_type: holding.account_type,
    account_name: holding.account_name,
  }));
};

export const createHolding = async (holding: HoldingRequest): Promise<void> => {
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

export const deleteHolding = async (holdingId: number): Promise<void> => {
  const token = localStorage.getItem("token");

  if (!token) {
    throw new Error("No token found");
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
