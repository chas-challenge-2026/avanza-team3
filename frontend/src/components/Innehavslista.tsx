import DataTable from "./DataTable";
import type { Holding } from "../types/Holding";
import useDashboard from "../hooks/useDasboard";
import styles from "./Innehavslista.module.css";

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
  const { dashboard } = useDashboard();
  const usdToSek = dashboard?.usdToSek ?? 1;

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
    valueSek: {
      field: "valueSek",
      headerName: "Värde (SEK)",
      render: (row: Holding) =>
        `${row.marketValue.toLocaleString("sv-SE", {
          maximumFractionDigits: 0,
        })} SEK`,
    },
    account_name: { field: "account_name", headerName: "Konto" },
    ticker: { field: "ticker", headerName: "Ticker" },
    instrumentName: { field: "instrumentName", headerName: "Instrument" },
    quantity: { field: "quantity", headerName: "Antal" },
    avgBuyPrice: {
      field: "avgBuyPrice",
      headerName: "Köppris",
      render: (row: Holding) =>
        `${row.avgBuyPrice.toLocaleString("sv-SE")} ${row.currency}`,
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
      render: (row: Holding) =>
        `${row.pnl.toLocaleString("sv-SE")} ${row.currency}`,
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
    <div>
      <DataTable
        rows={holdings}
        columns={tableColumns}
        title="Nuvarande innehav"
        width={width}
        showDelete={showDelete}
        onDelete={onDelete}
      />
      <p className={styles.currency}>
        Växelkurs: 1 USD = {usdToSek.toLocaleString("sv-SE")} SEK
      </p>
    </div>
  );
};

export default InnehavsLista;
