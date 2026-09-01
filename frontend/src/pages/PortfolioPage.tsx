import { accountRows, accountColumns } from "../data/accountsData";
import DataTable from "../components/DataTable";
import AppCard from "../components/AppCard";
import PortfolioHealth from "../components/PortfolioHealth";
import styles from "./PorfolioPage.module.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBriefcase } from "@fortawesome/free-solid-svg-icons";
import AllocationChart from "../components/AllocationChart";
import NotificationCard from "../components/NotificationCard";
import { faChartLine } from "@fortawesome/free-solid-svg-icons";

function PortfolioPage() {
  return (
    <div className={styles.container}>
      <div className={styles.row1}>
        <div className={styles.iconTitle}>
          <FontAwesomeIcon icon={faBriefcase} className={styles.icon} />
          <div className={styles.titleText}>
            <h1>Min Portfölj</h1>
            <h3 className={styles.label}>
              Översikt över din portfölj och tillgångar
            </h3>
          </div>
        </div>

        <AppCard>
          <div className={styles.header}>
            <FontAwesomeIcon icon={faChartLine} className={styles.icon} />
            <p className={styles.label}>Totalt värde (SEK)</p>
          </div>
          <p className={styles.value}>712 567 kr</p>
        </AppCard>
      </div>

      <div className={styles.row2}>
        <AllocationChart />
        <PortfolioHealth value={80} />
      </div>

      <div className={styles.row3}>
        <DataTable title="Konton" rows={accountRows} columns={accountColumns} />
        <NotificationCard />
      </div>
    </div>
  );
}

export default PortfolioPage;
