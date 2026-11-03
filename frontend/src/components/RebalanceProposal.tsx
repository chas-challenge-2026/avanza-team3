import AppCard from "./AppCard";
import AppButton from "./AppButton";
import type { RebalancePreviewResponse } from "../types/rebalance";

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
  return (
    <AppCard>
      <h2>Föreslagna affärer</h2>

      {preview.trades.length === 0 ? (
        <p>Inga affärer föreslås.</p>
      ) : (
        <div>
          {preview.trades.map((trade) => (
            <div key={`${trade.instrumentId}-${trade.side}`}>
              <h3>
                {trade.side === "BUY" ? "Köp" : "Sälj"} {trade.ticker}
              </h3>

              <p>Antal: {trade.quantity}</p>
              <p>Pris per enhet: {formatSek(trade.unitPriceSek)}</p>
              <p>Totalt: {formatSek(trade.amountSek)}</p>
            </div>
          ))}
        </div>
      )}

      <p>
        Tillgängligt saldo efter rebalansering:{" "}
        {formatSek(preview.cashAfterSek)}
      </p>

      {preview.warnings.length > 0 && (
        <div>
          <h3>Information</h3>
          {preview.warnings.map((warning, index) => (
            <p key={index}>{warning}</p>
          ))}
        </div>
      )}

      <AppButton
        variant="contained"
        onClick={onConfirm}
        disabled={!preview.executable || !preview.proposalToken || confirming}
      >
        {confirming ? "Genomför..." : "Bekräfta rebalansering"}
      </AppButton>
    </AppCard>
  );
};

export default RebalanceProposal;
