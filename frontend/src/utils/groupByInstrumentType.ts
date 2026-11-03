import type { HoldingSummary } from "../services/holdingSummaryService";

const colors: Record<string, string> = {
  Aktie: "#16a34a",
  Fond: "#1976d2",
};

export function groupByInstrumentType(holdings: HoldingSummary[]) {
  const totals: Record<string, number> = {};

  for (const holding of holdings) {
    totals[holding.instrument_type] =
      (totals[holding.instrument_type] ?? 0) + holding.marketValue;
  }

  return Object.entries(totals).map(([label, value]) => ({
    label,
    value,
    color: colors[label],
  }));
}
