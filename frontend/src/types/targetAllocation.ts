export type TargetAllocation = {
  id: number;
  accountType: string;
  targetPercentage: number;
};

export type TargetAllocationPatchRequest = {
  accountType?: string;
  targetPercentage?: number;
};
