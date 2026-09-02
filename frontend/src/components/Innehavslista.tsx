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
    if (holding.currentPrice > holding.avgBuyPrice) {
      return "over";
    } else if (holding.currentPrice < holding.avgBuyPrice) {
      return "under";
    } else {
      return "ok";
    }
  };

  const rowsWithBadgeStatus = holdings.map((holding) => {
    const badgeStatus = getPriceStatus(holding);
    return {
      ...holding,
      currentPrice: badgeStatus,
      label:
        badgeStatus === "under"
          ? "Över köppris"
          : badgeStatus === "over"
            ? "Under köppris"
            : "Lika köppris"
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
