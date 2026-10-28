import { useState } from "react";
import type { Instrument } from "../types/Instrument";
import { mockApprovedHoldings } from "../mock/mockApprovedHoldings.ts";

export function useApprovedHoldings() {
  const [approved, setApproved] = useState<Instrument[]>(mockApprovedHoldings);

  const addApproved = (instrument: Instrument) => {
    setApproved((prev) => [...prev, instrument]);
  };

  const removeApproved = (id: number) => {
    setApproved((prev) => prev.filter((i) => i.id !== id));
  };

  return { approved, addApproved, removeApproved };
}
