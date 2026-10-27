import InnehavsForm from "../components/InnehavForm";
import InnehavsLista from "../components/Innehavslista";
import styles from "./InnehavPage.module.css";
import { useHoldings } from "../hooks/useHoldings";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPiggyBank } from "@fortawesome/free-solid-svg-icons";
import { useApprovedHoldings } from "../hooks/useApprovedHoldings";
import ApprovedHoldingsList from "../components/ApprovedHoldingsList";

function InnehavPage() {
  const { holdings, loading, error, removeHolding } = useHoldings();
  const { approved, addApproved, removeApproved } = useApprovedHoldings();

  if (loading) return <p>Laddar...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div className={styles.InnehavPageWrapper}>
      <div className={styles.titleRow}>
        <div className={styles.iconTitle}>
          <FontAwesomeIcon icon={faPiggyBank} className={styles.icon} />
          <div className={styles.titleText}>
            <h1 className={styles.title}>Mina Innehav</h1>
          </div>
        </div>
      </div>
      {/*       <InnehavsLista
        holdings={holdings}
        columns={[
          "ticker",
          "instrumentName",
          "quantity",
          "avgBuyPrice",
          "valueSek",
          "account_type",
        ]}
        onDelete={removeHolding}
        // width="1200px"
        showDelete
      /> */}
      <ApprovedHoldingsList approved={approved} onRemove={removeApproved} />
      <InnehavsForm onAdd={addApproved} />
    </div>
  );
}

export default InnehavPage;
