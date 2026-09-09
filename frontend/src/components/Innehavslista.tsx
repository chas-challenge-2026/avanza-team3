import DataTable from "./DataTable";
import { type Holding } from "../data/mockData";
import { Paper, TableContainer } from "@mui/material";
import type { ReactNode } from "react";
import AppButton from "./AppButton";

type Column<T> = {
  field: string;
  headerName: string;
  render?: (row: T) => ReactNode;
  isBadge?: boolean;
};

type InnehavsListaProps = {
  holdings: Holding[];
  width?: string | number;
  showDelete?: boolean;
  onDelete?: (id: number) => void;
  detailed?: boolean;
};

const InnehavsLista = ({
  holdings,
  width,

  onDelete,
  detailed = false,
  showDelete
}: InnehavsListaProps) => {
  const columns: Column<Holding>[] = [
    { field: "ticker", headerName: "Ticker" },
    { field: "instrumentName", headerName: "Instrument" },
    { field: "accountId", headerName: "konto" },
    { field: "accountType", headerName: "typ" },
    { field: "quantity", headerName: "Antal" },
    { field: "avgBuyPrice", headerName: "Köppris" }
  ];

  if (detailed) {
    columns.push(
      { field: "currentPrice", headerName: "Aktuellt pris" },
      { field: "risk", headerName: "Risk" },
      { field: "allocation", headerName: "Allokering" },
      { field: "value", headerName: "Värde" }
    );
  }

  if (showDelete) {
    columns.push({
      field: "actions",
      headerName: "",
      render: (holdings: Holding) => (
        <AppButton
          color="error"
          variant="outlined"
          onClick={() => onDelete?.(holdings.id)}
        >
          Ta bort
        </AppButton>
      )
    });
  }
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
        rows={holdings}
        columns={columns}
        title="Nuvarande innehav"
      />
    </TableContainer>
  );
};
export default InnehavsLista;
