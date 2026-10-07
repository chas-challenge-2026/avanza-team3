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

function PortfolioPage() {
  const { rows } = usePortfolioAllocations();

  const { dashboard } = useDashboard();
  const accounts = dashboard?.accounts ?? [];
  const { liveAlerts } = useLiveAlerts();

  const accountColors: Record<string, string> = {
    ISK: "#16a34a",
    KF: "#1976d2",
    Depa: "#8b69a7"
  };

  const allocationData = rows.map((row) => ({
    label: row.accountType,
    value: row.actual,
    color: accountColors[row.accountType]
  }));

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
              title: "Vad är kontotyp",
              content:
                "Här ser du hur portföljens värde är fördelat mellan dina konton."
            }}
          />
        </AppCard>
        <PortfolioHealth
          value={80}
          help={{
            title: "Vad är portföljhälsa?",
            content:
              "Portföljhälsa ger en överblick över hur väl din portfölj är balanserad, till exempel utifrån risk, diversifiering och fördelning mellan innehav."
          }}
        />
      </div>

      <div className={styles.row3}>
        <DataTable title="Konton" rows={accounts} columns={accountColumns} />
        <NotificationCard alerts={liveAlerts} />
      </div>

      <div className={styles.row4}></div>
    </div>
  );
}

export default PortfolioPage;
