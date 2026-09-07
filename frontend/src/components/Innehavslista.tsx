import DataTable from "./DataTable";
import { holdings, type Holding } from "../data/mockData";
import { Button, Paper, TableContainer } from "@mui/material";
import { useState } from "react";

type InnehavsListaProps = {
  width?: string;
};

const InnehavsLista = ({ width }: InnehavsListaProps) => {
  const [currentHoldings, setCurrentHoldings] = useState(holdings);

  const holdingColumns = [
    { field: "id", headerName: "ID" },
    { field: "ticker", headerName: "Ticker" },
    { field: "instrumentName", headerName: "Instrument" },
    { field: "quantity", headerName: "Mängd" },
    { field: "avgBuyPrice", headerName: "Köppris" },
    {
      field: "currentPrice",
      headerName: "Aktuellt pris",
      isBadge: true
    },
    { field: "currency", headerName: "Valuta" },
    { field: "accountId", headerName: "Konto ID" },
    {
      field: "actions",
      headerName: "Åtgärder",
      render: (row: Holding) => (
        <Button
          variant="outlined"
          color="error"
          size="small"
          onClick={() => handleDelete(row.id)}
        >
          Ta bort
        </Button>
      )
    }
  ];

  const handleDelete = (id: number) => {
    setCurrentHoldings((previousHoldings) =>
      previousHoldings.filter((holding) => holding.id !== id)
    );
  };

  const getReturnStatus = (holding: Holding) => {
    // avkastning i procent
    const returnPercent = calculateReturnPercent(holding);

    if (returnPercent > 0) {
      return "over";
    } else if (returnPercent < 0) {
      return "under";
    } else {
      return "ok";
    }
  };

  const calculateReturnPercent = (holding: Holding) => {
    if (!holding.avgBuyPrice || holding.avgBuyPrice === 0) return 0;
    const procent =
      ((holding.currentPrice - holding.avgBuyPrice) / holding.avgBuyPrice) *
      100;
    return procent;
  };

  const rowsWithBadgeStatus = currentHoldings.map((holding) => {
    const badgeStatus = getReturnStatus(holding);
    const returnPercent = calculateReturnPercent(holding);

    // akutuellt värde
    const currentValue = holding.currentPrice * holding.quantity;
    // investerat värde
    const investedValue = holding.avgBuyPrice * holding.quantity;
    // avkastning
    const profit = currentValue - investedValue;

    return {
      ...holding,
      currentPrice: badgeStatus,
      label: `${returnPercent > 0 ? "+" : ""}${returnPercent.toFixed(1)}%`
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
