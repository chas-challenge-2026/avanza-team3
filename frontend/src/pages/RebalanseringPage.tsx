import InnehavsForm from "../components/InnehavForm";
import styles from "./RebalanseringPage.module.css";
import { useHoldings } from "../hooks/useHoldings";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faScaleBalanced } from "@fortawesome/free-solid-svg-icons";
import { useApprovedHoldings } from "../hooks/useApprovedHoldings";
import ApprovedHoldingsList from "../components/ApprovedHoldingsList";
import AccountPicker from "../components/AccountPicker";
import { useState } from "react";

function RebalanseringPage() {
  const { loading, error } = useHoldings();
  const { approved, addApproved, removeApproved } = useApprovedHoldings();
  const [selectedAccountId, setSelectedAccountId] = useState<number | null>(
    null,
  );

  return (
    <div className={styles.RebalanseringWrapper}>
      <div className={styles.titleRow}>
        <div className={styles.iconTitle}>
          <FontAwesomeIcon icon={faScaleBalanced} className={styles.icon} />
          <div className={styles.titleText}>
            <h1 className={styles.title}>Rebalansering</h1>
          </div>
        </div>
      </div>
      <AccountPicker
        selectedAccountId={selectedAccountId}
        onSelect={setSelectedAccountId}
      />
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

export default RebalanseringPage;
