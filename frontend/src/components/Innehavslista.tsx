import DataTable from "./DataTable";
import { holdings } from "../data/mockData";
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
  { field: "currency", headerName: "Valuta" },
  { field: "accountId", headerName: "Konto ID" }
];

const InnehavsLista = ({ width }: InnehavsListaProps) => {
  return (
    <TableContainer component={Paper} sx={{ width: width || "fit-content" }}>
      <DataTable
        title="Nuvarande innehav"
        rows={holdings}
        columns={holdingColumns}
      />
    </TableContainer>
  );
};
export default InnehavsLista;
