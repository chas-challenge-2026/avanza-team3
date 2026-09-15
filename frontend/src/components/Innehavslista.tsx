// import DataTable from "./DataTable";
// import type { Holding } from "../types/Holding";
// import { Paper, TableContainer } from "@mui/material";
// import type { ReactNode } from "react";
// import AppButton from "./AppButton";

import DataTable from "./DataTable";
import type { Holding } from "../types/Holding";
type ColumnKey = keyof Holding;

type InnehavsListaProps = {
  holdings: Holding[];
  columns?: ColumnKey[];
};
const InnehavsLista = ({
  holdings,
  columns
}: InnehavsListaProps) => {
  const columnConfig: Record<
  ColumnKey,
  { field: ColumnKey; headerName: string }
> = {
  id: { field: "id", headerName: "ID" },
  accountId: { field: "accountId", headerName: "Konto" },
  ticker: { field: "ticker", headerName: "Ticker" },
  instrumentName: { field: "instrumentName", headerName: "Instrument" },
  quantity: { field: "quantity", headerName: "Antal" },
  avgBuyPrice: { field: "avgBuyPrice", headerName: "Köppris" },
  currentPrice: { field: "currentPrice", headerName: "Aktuellt pris" },
  currency: { field: "currency", headerName: "Valuta" }
};

const defaultColumnKeys: ColumnKey[] = [
  "ticker",
  "instrumentName",
  "quantity",
  "avgBuyPrice"
];

const selectedColumns = columns ?? defaultColumnKeys;

const tableColumns = selectedColumns.map(
  (column) => columnConfig[column]
);

  return (
    <DataTable
      rows={holdings}
      columns={tableColumns}
      title="Nuvarande innehav"
    />
  );
};

export default InnehavsLista;


// type Column<T> = {
//   field: string;
//   headerName: string;
//   render?: (row: T) => ReactNode;
//   isBadge?: boolean;
// };

// type InnehavsListaProps = {
//   holdings: Holding[];
//   width?: string | number;
//   showDelete?: boolean;
//   onDelete?: (id: number) => void;
//   detailed?: boolean;
// };

// const InnehavsLista = ({
//   holdings,
//   width,

//   onDelete,
//   detailed = false,
//   showDelete
// }: InnehavsListaProps) => {
//   const columns: Column<Holding>[] = [
//     { field: "ticker", headerName: "Ticker" },
//     { field: "instrumentName", headerName: "Instrument" },
//     { field: "accountId", headerName: "konto" },
//     { field: "accountType", headerName: "typ" },
//     { field: "quantity", headerName: "Antal" },
//     { field: "avgBuyPrice", headerName: "Köppris" }
//   ];

//   if (detailed) {
//     columns.push(
//       { field: "currentPrice", headerName: "Aktuellt pris" },
//       { field: "risk", headerName: "Risk" },
//       { field: "allocation", headerName: "Allokering" },
//       { field: "value", headerName: "Värde" }
//     );
//   }

//   if (showDelete) {
//     columns.push({
//       field: "actions",
//       headerName: "",
//       render: (holdings: Holding) => (
//         <AppButton
//           color="error"
//           variant="outlined"
//           onClick={() => onDelete?.(holdings.id)}
//         >
//           Ta bort
//         </AppButton>
//       )
//     });
//   }
//   return (
//     <TableContainer
//       component={Paper}
//       sx={{
//         width: "100%",
//         maxWidth: width || 1200,
//         minWidth: 600,
//         overflowX: "auto",
//         tableLayout: "fixed"
//       }}
//     >
//       <DataTable
//         width="100%"
//         rows={holdings}
//         columns={columns}
//         title="Nuvarande innehav"
//       />
//     </TableContainer>
//   );
// };
// export default InnehavsLista;
