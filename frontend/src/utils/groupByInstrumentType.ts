import type { HoldingSummary } from "../services/holdingSummaryService";

const assetClassByType: Record<string, string> = {
  STOCK: "Aktier",
  BOND: "Räntor",
};

const colors: Record<string, string> = {
  Aktier: "#16a34a",
  Räntor: "#1976d2",
  Övrigt: "#f59e0b",
};

export function groupByInstrumentType(
  holdings: HoldingSummary[],
  typeById: Record<number, string>,
) {
  const totals: Record<string, number> = {};

  for (const holding of holdings) {
    const rawType = typeById[holding.instrument_id];
    const assetClass = assetClassByType[rawType] ?? "Övrigt";

    totals[assetClass] = (totals[assetClass] ?? 0) + holding.marketValue;
  }

  return Object.entries(totals).map(([label, value]) => ({
    label,
    value,
    color: colors[label],
  }));
}
