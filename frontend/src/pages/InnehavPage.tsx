import InnehavsForm from "../components/InnehavForm";
import styles from "./InnehavPage.module.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPiggyBank } from "@fortawesome/free-solid-svg-icons";
import { useApprovedHoldings } from "../hooks/useApprovedHoldings";
import ApprovedHoldingsList from "../components/ApprovedHoldingsList";
import AccountPicker from "../components/AccountPicker";
import { useState } from "react";

function InnehavPage() {
  const { approved, addApproved, removeApproved } = useApprovedHoldings();
  const [selectedAccountId, setSelectedAccountId] = useState<number | null>(
    null,
  );

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

export default InnehavPage;
