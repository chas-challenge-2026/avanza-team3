import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import {
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from "@mui/material";
import Badge from "./Badge";
import { formatCurrency } from "../utils/formatCurrency";
import AppButton from "./AppButton";
import { useState } from "react";

type BadgeVariant =
  | "over"
  | "under"
  | "ok"
  | "isk"
  | "kf"
  | "tjp"
  | "depa"
  | "aktie"
  | "drift";

type DataTableColumn<T> = {
  field: keyof T;
  headerName: string;
  isBadge?: boolean;
  render?: (row: T) => React.ReactNode;
};

type DataTableProps<T extends { id: number }> = {
  title?: string;
  rows: T[];
  columns: readonly DataTableColumn<T>[];
  width?: string;
  showDelete?: boolean;
  onDelete?: (id: number) => Promise<void>;
};

const DataTable = <T extends { id: number }>({
  title,
  rows,
  columns,
  width,
  showDelete,
  onDelete,
}: DataTableProps<T>) => {
  const [deleteId, setDeleteId] = useState<number | null>(null);

  return (
    <>
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
                  key={String(col.field)}
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
              {showDelete && <TableCell />}
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
                  <TableCell key={String(col.field)}>
                    {col.render ? (
                      col.render(row)
                    ) : col.isBadge ? (
                      <Badge
                        variant={
                          String(row[col.field]).toLowerCase() as BadgeVariant
                        }
                      >
                        {String(row[col.field])}
                      </Badge>
                    ) : col.field === "totalValueSek" ? (
                      formatCurrency(Number(row[col.field]))
                    ) : (
                      String(row[col.field] ?? "")
                    )}
                  </TableCell>
                ))}

                {showDelete && (
                  <TableCell>
                    <AppButton
                      color="warning"
                      onClick={() => setDeleteId(row.id)}
                    >
                      Ta bort
                    </AppButton>
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={deleteId !== null} onClose={() => setDeleteId(null)}>
        <DialogTitle>Ta bort innehav?</DialogTitle>

        <DialogContent>
          Detta innehav kommer att tas bort permanent. Är du säker på att du
          vill fortsätta?
        </DialogContent>

        <DialogActions>
          <AppButton onClick={() => setDeleteId(null)}>AVBRYT</AppButton>

          <AppButton
            color="primary"
            variant="outlined"
            onClick={() => {
              if (deleteId !== null) {
                onDelete?.(deleteId);
                setDeleteId(null);
              }
            }}
          >
            TA BORT
          </AppButton>
        </DialogActions>
      </Dialog>
    </>
  );
};

export default DataTable;
