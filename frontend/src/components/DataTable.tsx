import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import { Paper } from "@mui/material";
import Badge from "./Badge";

type DataTableProps = {
  title: string;
  rows: any[];
  columns: any[];
  width?: string;
};

const DataTable = ({ title, rows, columns, width }: DataTableProps) => {
  return (
    <TableContainer component={Paper} sx={{ width: width || "fit-content" }}>
      <h3>{title}</h3>
      <Table size="small">
        <TableHead>
          <TableRow>
            {columns.map((col) => (
              <TableCell key={col.field}>{col.headerName}</TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row) => (
            <TableRow key={row.id}>
              {columns.map((col) => (
                <TableCell key={col.field}>
                  {col.isBadge ? (
                    <Badge variant={row[col.field].toLowerCase()}>
                      {row.label}
                    </Badge>
                  ) : (
                    row[col.field]
                  )}
                </TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default DataTable;
