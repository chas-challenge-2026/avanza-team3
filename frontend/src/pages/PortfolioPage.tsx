import { accountColumns } from "../data/accountsData";
import DataTable from "../components/DataTable";
import AppCard from "../components/AppCard";
import PortfolioHealth from "../components/PortfolioHealth";
import styles from "./PorfolioPage.module.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBriefcase } from "@fortawesome/free-solid-svg-icons";
import DonutChart from "../components/DonutChart";
import NotificationCard from "../components/NotificationCard";
import { faChartLine } from "@fortawesome/free-solid-svg-icons";
import usePortfolioAllocations from "../hooks/usePortfolioAllocation";
import useDashboard from "../hooks/useDasboard";
import useLiveAlerts from "../hooks/useLiveAlerts";
import { usePortfolioRisk } from "../hooks/usePortfolioRisk";
import { calculatePortfolioHealth } from "../utils/calculatePortfolioHealth";
import { calculateHoldingSpread } from "../utils/calculateHoldingSpread";
import { useState, useEffect } from "react";
import { getHoldingsSummary } from "../services/holdingSummaryService";
import type { HoldingSummary } from "../services/holdingSummaryService";
import { useTargetAllocations } from "../hooks/useTargetAllocation";

function PortfolioPage() {
  const { rows } = usePortfolioAllocations();
  const { targetAllocation } = useTargetAllocations();
  const { dashboard } = useDashboard();
  const accounts = dashboard?.accounts ?? [];
  const { liveAlerts } = useLiveAlerts();
  const { riskData } = usePortfolioRisk();

  const [summaryHoldings, setSummaryHoldings] = useState<
    HoldingSummary[] | null
  >(null);

  useEffect(() => {
    getHoldingsSummary().then(setSummaryHoldings).catch(console.error);
  }, []);

  const accountColors: Record<string, string> = {
    ISK: "#6366F1",
    KF: "#0D9488",
    Depa: "#D97706"
  };

  const allocationData = rows
    .filter((row) => !row.accountType.toLowerCase().includes("pension"))
    .map((row) => {
      const currentTarget = targetAllocation.find(
        (target) => target.accountType === row.accountType
      );

      return {
        label: row.accountType,
        value: row.actual,
        color: accountColors[row.accountType]
        // target: currentTarget?.targetPercentage ?? row.target
      };
    });

  const riskScore = riskData?.current.riskScore;

  const spread = summaryHoldings
    ? calculateHoldingSpread(
        summaryHoldings.map((holding) => ({
          ticker: holding.ticker,
          valueSek: holding.marketValue
        }))
      )
    : undefined;

  const portfolioHealth =
    riskScore !== undefined && spread !== undefined
      ? calculatePortfolioHealth(riskScore, spread)
      : undefined;

  return (
    <div className={styles.container}>
      <div className={styles.titleRow}>
        <div className={styles.iconTitle}>
          <FontAwesomeIcon icon={faBriefcase} className={styles.icon} />
          <div className={styles.titleText}>
            <h1>Min Portfölj</h1>
            <h3 className={styles.label}>
              Översikt över din portfölj och tillgångar
            </h3>
          </div>
        </div>
      </div>

      <AppCard>
        <div className={styles.kpiCard}>
          <div className={styles.header}>
            <FontAwesomeIcon icon={faChartLine} className={styles.icon} />
            <p className={styles.label}>Totalt värde (SEK)</p>
          </div>
          <p className={styles.value}>
            {dashboard?.totalPortfolioValue.toLocaleString("sv-se", {
              maximumFractionDigits: 0
            })}{" "}
            SEK
          </p>
        </div>
      </AppCard>
      <div className={styles.row2}>
        <AppCard>
          <DonutChart
            title="Fördelning per kontotyp"
            data={allocationData}
            help={{
              title: "Vad betyder kontotyp?",
              content:
                "Kontotyp visar hur ditt konto är upplagt. ISK, KF och depå är olika sätt att äga och hantera investeringar."
            }}
          />
        </AppCard>
        {portfolioHealth !== undefined &&
          riskScore !== undefined &&
          spread !== undefined && (
            <PortfolioHealth
              value={portfolioHealth}
              help={{
                title: "Vad betyder portföljhälsa?",
                content:
                  "Portföljhälsa ger en överblick över hur väl din portfölj är balanserad, till exempel utifrån risk, diversifiering och fördelning mellan innehav."
              }}
              riskScore={riskScore}
              spread={spread}
            />
          )}
      </div>

      <div className={styles.row3}>
        <DataTable title="Konton" rows={accounts} columns={accountColumns} />
        <NotificationCard alerts={liveAlerts} allocationRows={rows} />
      </div>

      <div className={styles.row4}></div>
    </div>
  );
}

export default PortfolioPage;
