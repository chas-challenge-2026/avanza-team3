import type {
  TargetAllocation,
  TargetAllocationPatchRequest,
} from "../types/targetAllocation";

export const getTargetAllocations = async (): Promise<TargetAllocation[]> => {
  const token = localStorage.getItem("token");

  if (!token) {
    throw new Error("Ingen token hittades");
  }

  const response = await fetch("/api/target-allocations", {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok) {
    throw new Error(`Kunde inte hämta målallokering ${response.status}`);
  }

  const data = await response.json();

  return data.content;
};

export const updateTargetAllocations = async (
  id: number,
  changes: TargetAllocationPatchRequest,
): Promise<TargetAllocation> => {
  const token = localStorage.getItem("token");

  if (!token) {
    throw new Error("Ingen token hittades");
  }

  const response = await fetch(`/api/target-allocations/${id}`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify(changes),
  });

  if (!response.ok) {
    throw new Error(`Kunde inte skapa målallokering: ${response.status}`);
  }
  return response.json();
};
