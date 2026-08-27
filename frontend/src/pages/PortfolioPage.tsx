import { accountRows, accountColumns } from "../data/accountsData";
import DataTable from "../components/DataTable";

function PortfolioPage() {
  return (
    <>
      <h1>Min Portfolio</h1>
      <DataTable
        width="600px"
        title="Konton"
        rows={accountRows}
        columns={accountColumns}
      />
    </>
  );
}

export default PortfolioPage;
