import AppCard from "../components/AppCard";
import styles from "./PortfolioHealthPage.module.css";

function PortfolioHealthPage() {
  let riskPoint = 30;

  const riskLevel = () => {
    if (riskPoint < 30) {
      return "good";
    } else if (riskPoint <= 60) {
      return "warning";
    } else {
      return "danger";
    }
  };

  const diversification = 0;

  const diversificationLevel = () => {
    if (diversification >= 70) {
      return "good";
    } else if (diversification >= 40) {
      return "warning";
    } else {
      return "danger";
    }
  };

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
            <p className={styles.label + " " + styles[riskLevel()]}>
              Måttlig risk
            </p>
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
            <p className={styles.label + " " + styles[diversificationLevel()]}>
              God spridning
            </p>
          </div>
        </AppCard>
      </div>
    </>
  );
}

export default PortfolioHealthPage;
