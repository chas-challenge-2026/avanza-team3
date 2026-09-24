import AppCard from "../components/AppCard";
import styles from "./PortfolioHealthPage.module.css";
import { Gauge } from "@mui/x-charts/Gauge";
import usePortfolioAllocations from "../hooks/usePortfolioAllocation";
import DonutChart from "../components/DonutChart";
import useDashboard from "../hooks/useDasboard";
import InnehavsLista from "../components/Innehavslista";
import { holdings } from "../data/mockData";
import AllocationHandler from "../components/AllocationHandler";

function PortfolioHealthPage() {
  const { rows } = usePortfolioAllocations();
  const { dashboard } = useDashboard();

  const statusColors = {
    good: "#16a34a",
    warning: "#d97706",
    danger: "#dc2626",
  };

  const changePercent = 5;

  const trendStatus = () => {
    return changePercent >= 0 ? "good" : "danger";
  };

  let riskPoint = 42;

  const riskLevel = () => {
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
    danger: "Hög Risk",
  };

  const diversification = 72;

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
    danger: "Låg spridning",
  };

  const allocationData = rows.map((row) => ({
    label: row.accountType,
    value: row.actual,
  }));

  return (
    <>
      <h1>Portföljhälsa</h1>
      <div className={styles.container}>
        <AppCard>
          <div className={styles.kpiCard + " " + styles[riskLevel()]}>
            <div className={styles.header}>
              <p className={styles.label}>Riskpoäng</p>
            </div>
            <div className={styles.row}>
              <div>
                <p className={styles.value}>{riskPoint}/100</p>
                <p className={styles.label + " " + styles[riskLevel()]}>
                  {riskLevelText[riskLevel()]}
                </p>
              </div>
              <Gauge
                sx={{
                  "& .MuiGauge-valueArc": { fill: statusColors[riskLevel()] },
                  "& .MuiGauge-valueText": { display: "none" },
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
                maximumFractionDigits: 0,
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
              <p className={styles.label}>Diversifieringsgrad</p>
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
                    fill: statusColors[diversificationLevel()],
                  },
                  "& .MuiGauge-valueText": { display: "none" },
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
        <AppCard sx={{ maxWidth: "500px" }}>
          <AllocationHandler />
        </AppCard>
      </div>
      <div className={styles.container}>
        <InnehavsLista
          holdings={holdings}
          columns={["ticker", "instrumentName", "quantity", "avgBuyPrice"]}
        />
      </div>
      <div className={styles.conatiner}></div>
      <div className={styles.container}>
        <DonutChart title="Fördelning per kontotyp" data={allocationData} />
      </div>
    </>
  );
}

export default PortfolioHealthPage;
