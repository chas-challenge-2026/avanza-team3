import AppCard from "./AppCard";
import AppButton from "./AppButton";
import type { RebalancePreviewResponse } from "../types/rebalance";
import styles from "./RebalanceProposal.module.css";

type Props = {
  preview: RebalancePreviewResponse;
  onConfirm: () => void;
  confirming: boolean;
};

const formatSek = (value: number) =>
  value.toLocaleString("sv-SE", {
    style: "currency",
    currency: "SEK",
  });

const RebalanceProposal = ({ preview, onConfirm, confirming }: Props) => {
  const buys = preview.trades.filter((trade) => trade.side === "BUY");
  const sells = preview.trades.filter((trade) => trade.side === "SELL");

  return (
    <AppCard>
      <div className={styles.container}>
        <div className={styles.header}>
          <h2 className={styles.title}>Föreslagna affärer</h2>
          <p className={styles.subtitle}>
            Granska affärerna innan du bekräftar rebalanseringen.
          </p>
        </div>

        {preview.trades.length === 0 ? (
          <p className={styles.empty}>Inga affärer föreslås.</p>
        ) : (
          <div className={styles.tradeGrid}>
            {sells.map((trade) => (
              <div
                key={`${trade.instrumentId}-${trade.side}`}
                className={styles.tradeCard}
              >
                <div className={styles.tradeHeader}>
                  <span className={`${styles.badge} ${styles.sell}`}>SÄLJ</span>
                  <h3>{trade.ticker}</h3>
                </div>

                <div className={styles.tradeDetails}>
                  <div>
                    <span>Antal</span>
                    <strong>{trade.quantity}</strong>
                  </div>
                  <div>
                    <span>Pris per enhet</span>
                    <strong>{formatSek(trade.unitPriceSek)}</strong>
                  </div>
                </div>

                <div className={styles.tradeTotal}>
                  <span>Totalt säljbelopp</span>
                  <strong>{formatSek(trade.amountSek)}</strong>
                </div>
              </div>
            ))}

            {buys.map((trade) => (
              <div
                key={`${trade.instrumentId}-${trade.side}`}
                className={styles.tradeCard}
              >
                <div className={styles.tradeHeader}>
                  <span className={`${styles.badge} ${styles.buy}`}>KÖP</span>
                  <h3>{trade.ticker}</h3>
                </div>

                <div className={styles.tradeDetails}>
                  <div>
                    <span>Antal</span>
                    <strong>{trade.quantity}</strong>
                  </div>
                  <div>
                    <span>Pris per enhet</span>
                    <strong>{formatSek(trade.unitPriceSek)}</strong>
                  </div>
                </div>

                <div className={styles.tradeTotal}>
                  <span>Totalt köpbelopp</span>
                  <strong>{formatSek(trade.amountSek)}</strong>
                </div>
              </div>
            ))}
          </div>
        )}

        <div className={styles.summary}>
          <span>Tillgängligt saldo efter rebalansering</span>
          <strong>{formatSek(preview.cashAfterSek)}</strong>
        </div>

        <div className={styles.info}>
          {preview.warnings
            .filter(
              (warning) =>
                !warning.toLowerCase().includes("fallback") &&
                !warning.toLowerCase().includes("simulation prices") &&
                !warning.toLowerCase().includes("no real orders"),
            )
            .map((warning, index) => (
              <p key={index}>{warning}</p>
            ))}
        </div>

        <div className={styles.actions}>
          <AppButton
            variant="contained"
            onClick={onConfirm}
            disabled={
              !preview.executable || !preview.proposalToken || confirming
            }
          >
            {confirming ? "Genomför..." : "Bekräfta rebalansering"}
          </AppButton>
        </div>
      </div>
    </AppCard>
  );
};

export default RebalanceProposal;
