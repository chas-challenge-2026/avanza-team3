export type AssetCategory = "STOCK" | "FUND" | "BOND" | "OTHER";

export type EditableAssetCategory = Exclude<AssetCategory, "FUND">;

export type AccountAllocationTarget = {
  category: AssetCategory;
  targetPercentage: number;
};

export type AccountAllocationTargetsResponse = {
  accountId: number;
  targets: AccountAllocationTarget[];
};

export type ReplaceAccountTargetsRequest = {
  targets: {
    category: EditableAssetCategory;
    targetPercentage: number;
  }[];
};
