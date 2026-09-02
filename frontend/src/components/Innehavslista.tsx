import DataTable from "./DataTable";
import { holdings, type Holding } from "../data/mockData";
import { Paper, TableContainer } from "@mui/material";

type InnehavsListaProps = {
  width?: string;
};

const holdingColumns = [
  { field: "id", headerName: "ID" },
  { field: "ticker", headerName: "Ticker" },
  { field: "instrumentName", headerName: "Instrument" },
  { field: "quantity", headerName: "Mängd" },
  { field: "avgBuyPrice", headerName: "Köppris" },
  {
    field: "currentPrice",
    headerName: "Aktuellt värde",
    isBadge: true
  },
  { field: "currency", headerName: "Valuta" },
  { field: "accountId", headerName: "Konto ID" }
];

const InnehavsLista = ({ width }: InnehavsListaProps) => {
  const getPriceStatus = (holding: Holding) => {
    const deltaPercent = toPercent(holding);

    if (deltaPercent > 0) {
      return "over";
    } else if (deltaPercent < 0) {
      return "under";
    } else {
      return "ok";
    }
  };

  const toPercent = (holding: Holding) => {
    if (!holding.avgBuyPrice || holding.avgBuyPrice === 0) return 0;
    const procent =
      ((holding.currentPrice - holding.avgBuyPrice) / holding.avgBuyPrice) *
      100;
    return procent;
  };

  const rowsWithBadgeStatus = holdings.map((holding) => {
    const badgeStatus = getPriceStatus(holding);
    const deltaPercent = toPercent(holding);

    return {
      ...holding,
      currentPrice: badgeStatus,
      label: `${deltaPercent > 0 ? "+" : ""}${deltaPercent.toFixed(1)}%`
    };
  });

  return (
    <TableContainer
      component={Paper}
      sx={{
        width: "100%",
        maxWidth: width || 1200,
        minWidth: 600,
        overflowX: "auto",
        tableLayout: "fixed"
      }}
    >
      <DataTable
        width="100%"
        title="Nuvarande innehav"
        rows={rowsWithBadgeStatus}
        columns={holdingColumns}
      />
    </TableContainer>
  );
};
export default InnehavsLista;
