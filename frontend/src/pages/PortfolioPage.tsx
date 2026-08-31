import { accountRows, accountColumns } from "../data/accountsData";
import DataTable from "../components/DataTable";
import AppCard from "../components/AppCard";
import PortfolioHealth from "../components/PortfolioHealth";
import styles from "./PorfolioPage.module.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBriefcase } from "@fortawesome/free-solid-svg-icons";
import NotificationCard from "../components/NotificationCard";

function PortfolioPage() {
  return (
    <>
      <div className={styles.container}>
        {/* Column 1 */}
        <div className={styles.col1}>
          <div className={styles.iconTitle}>
            <FontAwesomeIcon icon={faBriefcase} className={styles.icon} />
            <div className={styles.titleText}>
              <h1>Min Portfölj</h1>
              <h3 className={styles.label}>
                Översikt över din portfölj och tillgångar
              </h3>
            </div>
          </div>
          <AppCard sx={{ maxWidth: 250 }}>
            <p className={styles.label}>Totalt värde (SEK)</p>
            <p className={styles.value}>712 567</p>
          </AppCard>

          <DataTable
            width="600px"
            title="Konton"
            rows={accountRows}
            columns={accountColumns}
          />
        </div>

        {/* Column 2 */}
        <div className={styles.col2}>
          <PortfolioHealth value={80} />

          <NotificationCard />
        </div>
      </div>
    </>
  );
}

export default PortfolioPage;
