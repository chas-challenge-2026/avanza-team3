import type { Instrument } from "../types/Instrument";

export const getInstruments = async (): Promise<Instrument[]> => {
  const token = localStorage.getItem("token");

  const response = await fetch("/api/instruments", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error("Kunde inte hämta instrument");
  }

  return response.json();
};