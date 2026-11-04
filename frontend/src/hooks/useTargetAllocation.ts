import { useEffect, useState } from "react";
import type {
  TargetAllocation,
  TargetAllocationPatchRequest
} from "../types/targetAllocation";
import {
  getTargetAllocations,
  updateTargetAllocations
} from "../services/targetAllocationsService";

export const useTargetAllocations = () => {
  const [targetAllocation, setTargetAllocations] = useState<TargetAllocation[]>(
    []
  );
  const [loading, setLoading] = useState(true);
  const [, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadTargets = async () => {
      try {
        const data = await getTargetAllocations();
        setTargetAllocations(data);
      } catch {
        setError("Kunde inte hämta målallokering");
      } finally {
        setLoading(false);
      }
    };
    void loadTargets();
  }, []);

  const editTargetAllocation = async (
    id: number,
    changes: TargetAllocationPatchRequest
  ) => {
    try {
      const updatedTarget = await updateTargetAllocations(id, changes);

      setTargetAllocations((current) =>
        current.map((target) => (target.id === id ? updatedTarget : target))
      );
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Kunde inte uppdatera målallokering"
      );
      throw error;
    }
  };
  return { targetAllocation, editTargetAllocation, loading };
};
