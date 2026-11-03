import DonutChart from "./DonutChart";
import styles from "./AllocationSuggestion.module.css";
import { useAccountAllocation } from "../hooks/useAccountAllocation";
import type { RebalancePreviewResponse } from "../types/rebalance";

const categoryConfig: Record<string, { label: string; color: string }> = {
  STOCK: { label: "Aktier", color: "#3b82f6" },
  FUND: { label: "Fonder", color: "#22c55e" },
  BOND: { label: "Räntor", color: "#f59e0b" },
  OTHER: { label: "Övrigt", color: "#94a3b8" },
};

type AllocationSuggestionProps = {
  accountId: number;
  preview: RebalancePreviewResponse | null;
};

const AllocationSuggestion = ({
  accountId,
  preview,
}: AllocationSuggestionProps) => {
  const { allocation, loading, error } = useAccountAllocation(accountId);

  if (loading) return <p>Laddar allokering...</p>;
  if (error) return <p>{error}</p>;
  if (!allocation) return null;

  const targetAllocation = allocation.categories
    .filter((item) => (item.targetPct ?? 0) > 0)
    .map((item) => ({
      label: categoryConfig[item.category]?.label ?? item.category,
      value: item.targetPct ?? 0,
      color: categoryConfig[item.category]?.color ?? "#94a3b8",
    }));

  const currentAllocation = allocation.categories
    .filter((item) => item.valueSek > 0)
    .map((item) => ({
      label: categoryConfig[item.category]?.label ?? item.category,
      value: item.valueSek,
      color: categoryConfig[item.category]?.color ?? "#94a3b8",
    }));

  const suggestedAllocation = (preview?.after ?? [])
    .filter((item) => item.valueSek > 0)
    .map((item) => ({
      label: categoryConfig[item.category]?.label ?? item.category,
      value: item.valueSek,
      color: categoryConfig[item.category]?.color ?? "#94a3b8",
    }));

  return (
    <div className={styles.container}>
      <DonutChart
        title="Nuvarande fördelning"
        data={currentAllocation}
        help={{
          title: "Nuvarande fördelning",
          content: "Så här är dina tillgångar fördelade idag.",
        }}
      />

      <DonutChart
        title={preview ? "Föreslagen fördelning" : "Målallokering"}
        data={preview ? suggestedAllocation : targetAllocation}
        help={{
          title: preview ? "Föreslagen fördelning" : "Målallokering",
          content: preview
            ? "Så här beräknas kontot vara fördelat efter rebalansering."
            : "Så här vill du att kontot ska vara fördelat.",
        }}
      />
    </div>
  );
};

export default AllocationSuggestion;
