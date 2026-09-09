import AppCard from "../components/AppCard";
import styles from "./PortfolioHealthPage.module.css";

function PortfolioHealthPage() {
  return (
    <>
      <h1>Portföljhälsa</h1>
      <div className={styles.container}>
        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Totalt värde (SEK)</p>
            </div>
            <p className={styles.value}>712 567 kr</p>
          </div>
        </AppCard>
        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Riskpoäng</p>
            </div>
            <p className={styles.value}>42/100</p>
          </div>
        </AppCard>
        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Antal innehav</p>
            </div>
            <p className={styles.value}>18 st</p>
          </div>
        </AppCard>
        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Diversifieringsgrad</p>
            </div>
            <p className={styles.value}>72%</p>
            <p className={styles.label}>God spridning</p>
          </div>
        </AppCard>
      </div>
    </>
  );
}

export default PortfolioHealthPage;
