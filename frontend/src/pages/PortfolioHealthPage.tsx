import AppCard from "../components/AppCard";
import styles from "./PortfolioHealthPage.module.css";

function PortfolioHealthPage() {
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

  return (
    <>
      <h1>Portföljhälsa</h1>
      <div className={styles.container}>
        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Totalt Portföljvärde</p>
            </div>
            <p className={styles.value}>712 567 SEK</p>
            <p className={styles.label + " " + styles[trendStatus()]}>
              {changePercent >= 0 ? "+" : ""}
              {changePercent}% i år
            </p>
          </div>
        </AppCard>
        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Riskpoäng</p>
            </div>
            <p className={styles.value}>42/100</p>
            <p className={styles.label + " " + styles[riskLevel()]}>
              {riskLevelText[riskLevel()]}
            </p>
          </div>
        </AppCard>
        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Antal innehav</p>
            </div>
            <p className={styles.value}>18 st</p>
            <p className={styles.label}>12 fonder, 6 aktier</p>
          </div>
        </AppCard>
        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Diversifieringsgrad</p>
            </div>
            <p className={styles.value}>72%</p>
            <p className={styles.label + " " + styles[diversificationLevel()]}>
              {diversificationText[diversificationLevel()]}
            </p>
          </div>
        </AppCard>
      </div>
    </>
  );
}

export default PortfolioHealthPage;
