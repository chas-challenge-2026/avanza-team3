import DataTable from "./DataTable";
import type { Holding } from "../types/Holding";
type ColumnKey = keyof Holding;

type InnehavsListaProps = {
  holdings: Holding[];
  columns?: ColumnKey[];
  width?: string;
  showDelete?: boolean;
  onDelete?: (id: number) => Promise<void>;
};
const InnehavsLista = ({
  holdings,
  columns,
  width,
  showDelete,
  onDelete,
}: InnehavsListaProps) => {
  const columnConfig: Record<
    ColumnKey,
    {
      field: ColumnKey;
      headerName: string;
      isBadge?: boolean;
      render?: (row: Holding) => string;
    }
  > = {
    id: { field: "id", headerName: "ID" },
    accountId: { field: "accountId", headerName: "Konto" },
    marketValue: {
      field: "marketValue",
      headerName: "Marknadsvärde",
      render: (row: Holding) =>
        `${row.marketValue.toLocaleString("sv-SE")} SEK`,
    },
    account_name: { field: "account_name", headerName: "Konto" },
    ticker: { field: "ticker", headerName: "Ticker" },
    instrumentName: { field: "instrumentName", headerName: "Instrument" },
    quantity: { field: "quantity", headerName: "Antal" },
    avgBuyPrice: {
      field: "avgBuyPrice",
      headerName: "Köppris",
      render: (row: Holding) =>
        `${row.avgBuyPrice.toLocaleString("sv-SE")} SEK`,
    },
    currentPrice: { field: "currentPrice", headerName: "Aktuellt pris" },
    currency: { field: "currency", headerName: "Valuta" },
    account_type: {
      field: "account_type",
      headerName: "Kontotyp",
      isBadge: true,
    },
    pnl: {
      field: "pnl",
      headerName: "Vinst/Förlust",
      render: (row: Holding) => `${row.pnl.toLocaleString("sv-SE")} SEK`,
    },
    pnlPct: {
      field: "pnlPct",
      headerName: "Vinst/Förlust %",
      render: (row: Holding) => `${row.pnlPct}%`,
    },
  };

  const defaultColumnKeys: ColumnKey[] = [
    "ticker",
    "instrumentName",
    "quantity",
    "avgBuyPrice",
  ];

  const selectedColumns = columns ?? defaultColumnKeys;

  const tableColumns = selectedColumns.map((column) => columnConfig[column]);

  return (
    <DataTable
      rows={holdings}
      columns={tableColumns}
      title="Nuvarande innehav"
      width={width}
      showDelete={showDelete}
      onDelete={onDelete}
    />
  );
};

export default InnehavsLista;
