import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import { Paper } from "@mui/material";
import Badge from "./Badge";

type DataTableProps = {
  title?: string;
  rows: any[];
  columns: any[];
  width?: string;
};

const DataTable = ({ title, rows, columns, width }: DataTableProps) => {
  return (
    <TableContainer
      component={Paper}
      elevation={0}
      sx={{
        width: width || "100%",
        padding: 3,
        borderRadius: "12px",
        border: "1px solid var(--border)",
      }}
    >
      <h2>{title}</h2>
      <Table size="small">
        <TableHead>
          <TableRow sx={{ backgroundColor: "var(--secondary)" }}>
            {columns.map((col) => (
              <TableCell
                key={col.field}
                sx={{
                  fontSize: "0.75rem",
                  fontWeight: 700,
                  textTransform: "uppercase",
                  letterSpacing: "0.04em",
                  color: "var(--muted-foreground)",
                  borderBottom: "1px solid var(--border)",
                }}
              >
                {col.headerName}
              </TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row) => (
            <TableRow
              key={row.id}
              sx={{
                "&:hover": { backgroundColor: "#f8faf9" },
                "&:last-child td": { borderBottom: "none" },
              }}
            >
              {columns.map((col) => (
                <TableCell key={col.field}>
                  {col.render ? (
                    col.render(row)
                  ) : col.isBadge ? (
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
