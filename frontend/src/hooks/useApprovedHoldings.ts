import { useState } from "react";
import type { Instrument } from "../types/Instrument";

export function useApprovedHoldings() {
  const [approved, setApproved] = useState<Instrument[]>([]);

  const addApproved = (instrument: Instrument) => {
    setApproved((prev) =>
      prev.some((i) => i.id === instrument.id)
        ? prev
        : [...prev, instrument],
    );
  };

  const removeApproved = (id: number) => {
    setApproved((prev) => prev.filter((i) => i.id !== id));
  };

  return { approved, addApproved, removeApproved };
}