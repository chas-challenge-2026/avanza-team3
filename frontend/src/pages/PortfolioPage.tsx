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
import CurrencyExposure from "../components/CurrencyExposure";
import usePortfolioAllocations from "../hooks/usePortfolioAllocation";
import useDashboard from "../hooks/useDasboard";
import useLiveAlerts from "../hooks/useLiveAlerts";

function PortfolioPage() {
  const { rows } = usePortfolioAllocations();

  const { dashboard } = useDashboard();
  const accounts = dashboard?.accounts ?? [];
  const { liveAlerts } = useLiveAlerts();

  const allocationData = rows.map((row) => ({
    label: row.accountType,
    value: row.actual,
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
          <p className={styles.value}>712 568 kr</p>
        </div>
      </AppCard>

      <div className={styles.row2}>
        <DonutChart title="Fördelning per kontotyp" data={allocationData} />
        <PortfolioHealth value={80} />
      </div>

      <div className={styles.row3}>
        <DataTable title="Konton" rows={accounts} columns={accountColumns} />
        <NotificationCard alerts={liveAlerts} />
      </div>

      <div className={styles.row4}>
        <CurrencyExposure />
      </div>
    </div>
  );
}

export default PortfolioPage;
