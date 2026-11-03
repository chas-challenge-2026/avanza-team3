import InnehavsForm from "../components/InnehavForm";
import styles from "./RebalanseringPage.module.css";
// import { useHoldings } from "../hooks/useHoldings";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faScaleBalanced } from "@fortawesome/free-solid-svg-icons";
import { useApprovedHoldings } from "../hooks/useApprovedHoldings";
import ApprovedHoldingsList from "../components/ApprovedHoldingsList";
import AccountPicker from "../components/AccountPicker";
import { useState } from "react";
import AllocationSuggestion from "../components/AllocationSuggestion";
import {
  previewRebalance,
  confirmRebalance,
} from "../services/rebalanceService";
import type { RebalancePreviewResponse } from "../types/rebalance";
import AppButton from "../components/AppButton";
import RebalanceProposal from "../components/RebalanceProposal";

function RebalanseringPage() {
  // const { loading, error } = useHoldings();
  const { approved, addApproved, removeApproved } = useApprovedHoldings();
  const [selectedAccountId, setSelectedAccountId] = useState<number | null>(
    null,
  );
  const [preview, setPreview] = useState<RebalancePreviewResponse | null>(null);

  const [previewLoading, setPreviewLoading] = useState(false);
  const [previewError, setPreviewError] = useState<string | null>(null);

  const [confirming, setConfirming] = useState(false);

  const handlePreview = async () => {
    if (selectedAccountId === null || approved.length === 0) return;

    setPreviewLoading(true);
    setPreviewError(null);
    setPreview(null);

    try {
      const result = await previewRebalance(
        selectedAccountId,
        approved.map((instrument) => instrument.id),
      );

      setPreview(result);
      console.log("REBALANCE PREVIEW:", result);
    } catch (error) {
      setPreviewError(
        error instanceof Error ? error.message : "Något gick fel",
      );
    } finally {
      setPreviewLoading(false);
    }
  };

  const handleConfirm = async () => {
    if (selectedAccountId === null || !preview?.proposalToken) return;

    setConfirming(true);
    setPreviewError(null);

    try {
      await confirmRebalance(selectedAccountId, preview.proposalToken);
      setPreview(null);
      alert("Rebalanseringen har genomförts!");
    } catch (error) {
      setPreviewError(
        error instanceof Error
          ? error.message
          : "Kunde inte genomföra rebalanseringen",
      );
    } finally {
      setConfirming(false);
    }
  };
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
        onSelect={(accountId) => {
          setSelectedAccountId(accountId);
          setPreview(null);
        }}
      />
      {selectedAccountId !== null && (
        <AllocationSuggestion accountId={selectedAccountId} preview={preview} />
      )}
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
      <ApprovedHoldingsList
        approved={approved}
        onRemove={(id) => {
          removeApproved(id);
          setPreview(null);
        }}
      />

      <InnehavsForm
        onAdd={(instrument) => {
          addApproved(instrument);
          setPreview(null);
        }}
      />
      <AppButton
        onClick={handlePreview}
        disabled={
          selectedAccountId === null || approved.length === 0 || previewLoading
        }
        variant="contained"
      >
        {previewLoading ? "Beräknar..." : "Beräkna rebalansering"}
      </AppButton>

      {preview && (
        <RebalanceProposal
          preview={preview}
          onConfirm={handleConfirm}
          confirming={confirming}
        />
      )}

      {previewError && <p>{previewError}</p>}
    </div>
  );
}

export default RebalanseringPage;
