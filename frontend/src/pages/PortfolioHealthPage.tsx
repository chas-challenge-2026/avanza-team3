import AppCard from "../components/AppCard";
import styles from "./PortfolioHealthPage.module.css";
import { Gauge } from "@mui/x-charts/Gauge";
// import usePortfolioAllocations from "../hooks/usePortfolioAllocation";
import DonutChart from "../components/DonutChart";
import useDashboard from "../hooks/useDasboard";
import InnehavsLista from "../components/Innehavslista";
import { useHoldings } from "../hooks/useHoldings";
import AllocationHandler from "../components/AllocationHandler";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStethoscope } from "@fortawesome/free-solid-svg-icons";
import CurrencyExposure from "../components/CurrencyExposure";
import PopOver from "../components/Popover";
import { usePortfolioRisk } from "../hooks/usePortfolioRisk";
import { calculateHoldingSpread } from "../utils/calculateHoldingSpread";
import { useEffect } from "react";
import { useTargetAllocations } from "../hooks/useTargetAllocation";
import usePortfolioAllocations from "../hooks/usePortfolioAllocation";

function PortfolioHealthPage() {
  const { rows } = usePortfolioAllocations();
  const { dashboard, loading } = useDashboard();
  const { holdings } = useHoldings();
  const { riskData } = usePortfolioRisk();
  const {
    targetAllocation,
    editTargetAllocation,
    loading: targetAllocationLoading
  } = useTargetAllocations();

  const statusColors = {
    good: "#16a34a",
    warning: "#d97706",
    danger: "#dc2626"
  };

  // const accountColors: Record<string, string> = {
  //   ISK: "#16a34a",
  //   KF: "#1976d2",
  //   Depa: "#8b69a7",
  // };
  useEffect(() => {
    if (!dashboard) return;

    const holdings = dashboard.holdings.content;

    console.table(
      holdings.map((holding) => ({
        ticker: holding.ticker,
        valueSek: holding.valueSek,
        currency: holding.currency
      }))
    );

    console.log(
      "Total SEK:",
      holdings.reduce((sum, holding) => sum + holding.valueSek, 0)
    );

    console.log("Spread:", calculateHoldingSpread(holdings));
  }, [dashboard]);

  const changePercent = 5;

  const trendStatus = () => {
    return changePercent >= 0 ? "good" : "danger";
  };

  const riskPoint = riskData?.current.riskScore;

  const riskLevel = () => {
    if (riskPoint === undefined) return "warning";

    if (riskPoint < 30) {
      return "good";
    } else if (riskPoint <= 60) {
      return "warning";
    } else {
      return "danger";
    }
  };

  const riskLevelText = {
    good: "Låg Risk",
    warning: "Måttlig Risk",
    danger: "Hög Risk"
  };

  const diversification = dashboard
    ? calculateHoldingSpread(dashboard.holdings.content)
    : 0;

  const diversificationLevel = () => {
    if (diversification >= 70) {
      return "good";
    } else if (diversification >= 40) {
      return "warning";
    } else {
      return "danger";
    }
  };

  const diversificationText = {
    good: "God spridning",
    warning: "Måttlig spridning",
    danger: "Låg spridning"
  };

  // const allocationData = rows.map((row) => ({
  //   label: row.accountType,
  //   value: row.actual,
  //   color: accountColors[row.accountType],
  // }));

  const allocations = targetAllocation.map((target) => ({
    id: String(target.id),
    label: target.accountType,
    value: target.targetPercentage
  }));

  const accountColors: Record<string, string> = {
    ISK: "#16a34a",
    KF: "#1976d2",
    Depa: "#8b69a7"
  };

  const allocationData = rows.map((row) => {
    const currentTarget = targetAllocation.find(
      (target) => target.accountType === row.accountType
    );

    return {
      label: row.accountType,
      value: row.actual,
      color: accountColors[row.accountType],
      target: currentTarget?.targetPercentage ?? row.target
    };
  });

  return (
    <>
      <div className={styles.container}>
        <div className={styles.titleRow}>
          <div className={styles.iconTitle}>
            <FontAwesomeIcon icon={faStethoscope} className={styles.icon} />
            <div className={styles.titleText}>
              <h1 className={styles.title}>Min Portföljhälsa</h1>
            </div>
          </div>
        </div>
      </div>
      <div className={styles.container}>
        <AppCard>
          <div className={styles.kpiCard + " " + styles[riskLevel()]}>
            <div className={styles.header}>
              <p className={styles.label}>Riskpoäng</p>
              <PopOver
                title="Vad betyder Riskpoäng?"
                content="Riskpoäng sammanfattar hur mycket värdet på en investering kan svänga och hur stor risken är för förlust. Ju högre poäng, desto större svängningar kan du behöva tåla."
              />
            </div>
            <div className={styles.row}>
              <div>
                <p className={styles.value}>
                  {riskPoint !== undefined ? `${riskPoint}/100` : "–"}
                </p>
                <p className={styles.label + " " + styles[riskLevel()]}>
                  {riskPoint !== undefined
                    ? riskLevelText[riskLevel()]
                    : "Ingen data"}
                </p>
              </div>
              <Gauge
                sx={{
                  "& .MuiGauge-valueArc": { fill: statusColors[riskLevel()] },
                  "& .MuiGauge-valueText": { display: "none" }
                }}
                width={90}
                height={90}
                innerRadius="40%"
                outerRadius="80%"
                value={riskPoint}
              />
            </div>
          </div>
        </AppCard>
        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Totalt Portföljvärde</p>
            </div>
            <p className={styles.value}>
              {dashboard?.totalPortfolioValue.toLocaleString("sv-se", {
                maximumFractionDigits: 0
              })}{" "}
              SEK
            </p>
            <p className={styles.label + " " + styles[trendStatus()]}>
              {changePercent >= 0 ? "+" : ""}
              {changePercent}% i år
            </p>
          </div>
        </AppCard>

        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Antal innehav</p>
            </div>
            <p className={styles.value}>
              {dashboard?.holdings.totalElements} st
            </p>
          </div>
        </AppCard>
        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Innehavsspridning</p>
              <PopOver
                title="Vad betyder Innehavsspridning?"
                content="Innehavsspridning visar hur jämnt portföljens värde är fördelat mellan olika innehav.
                  Ett högre värde innebär att kapitalet är mer utspritt, 
                 men säger inte något om hur innehavens risker samvarierar."
              />
            </div>
            <div className={styles.row}>
              <div>
                <p className={styles.value}>{diversification}%</p>
                <p
                  className={
                    styles.label + " " + styles[diversificationLevel()]
                  }
                >
                  {diversificationText[diversificationLevel()]}
                </p>
              </div>
              <Gauge
                sx={{
                  "& .MuiGauge-valueArc": {
                    fill: statusColors[diversificationLevel()]
                  },
                  "& .MuiGauge-valueText": { display: "none" }
                }}
                width={90}
                height={60}
                value={diversification}
                startAngle={-90}
                endAngle={90}
                innerRadius="65%"
                outerRadius="100%"
              />
            </div>
          </div>
        </AppCard>
      </div>
      <div className={styles.container}>
        <AppCard>
          {!loading && !targetAllocationLoading && dashboard && (
            <AllocationHandler
              allocations={allocations}
              title="Målalloekering över kontotyp"
              onSave={async (values) => {
                await Promise.all(
                  targetAllocation.map((target) =>
                    editTargetAllocation(target.id, {
                      targetPercentage: values[String(target.id)]
                    })
                  )
                );
              }}
            />
          )}
        </AppCard>
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
      </div>

      <div className={styles.container}>
        <CurrencyExposure />
      </div>

      <div className={styles.container}>
        <InnehavsLista
          holdings={holdings}
          columns={[
            "ticker",
            "instrumentName",
            "quantity",
            "avgBuyPrice",
            "valueSek",
            "account_type",
            "account_name",
            "marketValue",
            "pnl",
            "pnlPct"
          ]}
        />
      </div>
    </>
  );
}

export default PortfolioHealthPage;
