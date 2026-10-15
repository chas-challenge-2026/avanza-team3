import { useEffect, useState } from "react";
import type { Instrument } from "../types/Instrument";
import { getInstruments } from "../services/instrumentService";

export const useInstruments = () => {
  const [instrument, setInstrument] = useState<Instrument[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadInstruments = async () => {
      try {
        const data = await getInstruments();
        setInstrument(data);
      } catch {
        setError("Kunde inte hämta instrument");
      } finally {
        setLoading(false);
      }
    };

    loadInstruments();
  }, []);

  return {
    instrument,
    loading,
    error
  };
};
