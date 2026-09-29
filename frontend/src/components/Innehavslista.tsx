import DataTable from "./DataTable";
import type { Holding } from "../types/Holding";
type ColumnKey = keyof Holding;

type InnehavsListaProps = {
  holdings: Holding[];
  columns?: ColumnKey[];
};
const InnehavsLista = ({ holdings, columns }: InnehavsListaProps) => {
  const columnConfig: Record<
    ColumnKey,
    { field: ColumnKey; headerName: string; isBadge?: Boolean }
  > = {
    id: { field: "id", headerName: "ID" },
    accountId: { field: "accountId", headerName: "Konto" },
    marketValue: { field: "marketValue", headerName: "Marknadsvärde" },
    account_name: { field: "account_name", headerName: "Konto" },
    ticker: { field: "ticker", headerName: "Ticker" },
    instrumentName: { field: "instrumentName", headerName: "Instrument" },
    quantity: { field: "quantity", headerName: "Antal" },
    avgBuyPrice: { field: "avgBuyPrice", headerName: "Köppris" },
    currentPrice: { field: "currentPrice", headerName: "Aktuellt pris" },
    currency: { field: "currency", headerName: "Valuta" },
    account_type: {
      field: "account_type",
      headerName: "Kontotyp",
      isBadge: true
    }
  };

  const defaultColumnKeys: ColumnKey[] = [
    "ticker",
    "instrumentName",
    "quantity",
    "avgBuyPrice"
  ];

  const selectedColumns = columns ?? defaultColumnKeys;

  const tableColumns = selectedColumns.map((column) => columnConfig[column]);

  return (
    <DataTable
      rows={holdings}
      columns={tableColumns}
      title="Nuvarande innehav"
    />
  );
};

export default InnehavsLista;
