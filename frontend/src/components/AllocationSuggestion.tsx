import DonutChart from "./DonutChart";
import styles from "./AllocationSuggestion.module.css";
import { useAccountAllocation } from "../hooks/useAccountAllocation";

const categoryConfig: Record<string, { label: string; color: string }> = {
  STOCK: { label: "Aktier", color: "#3b82f6" },
  FUND: { label: "Fonder", color: "#22c55e" },
  BOND: { label: "Räntor", color: "#f59e0b" },
  OTHER: { label: "Övrigt", color: "#94a3b8" },
};

type AllocationSuggestionProps = {
  accountId: number;
};

const AllocationSuggestion = ({ accountId }: AllocationSuggestionProps) => {
  const { allocation, loading, error } = useAccountAllocation(accountId);

  if (loading) return <p>Laddar allokering...</p>;
  if (error) return <p>{error}</p>;
  if (!allocation) return null;

  console.log("ACCOUNT ALLOCATION:", allocation);

  const suggestedAllocation = [
    { label: "Aktier", value: 50, color: "#3b82f6" },
    { label: "Fonder", value: 25, color: "#22c55e" },
    { label: "Räntor", value: 15, color: "#f59e0b" },
    { label: "Övrigt", value: 10, color: "#94a3b8" },
  ];

  const currentAllocation = allocation.categories
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
        title="Föreslagen fördelning"
        data={suggestedAllocation}
        help={{
          title: "Föreslagen fördelning",
          content:
            "Så här skulle dina tillgångar fördelas efter ombalansering.",
        }}
      />
    </div>
  );
};

export default AllocationSuggestion;
