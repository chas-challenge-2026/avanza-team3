import AppCard from "./AppCard";
import styles from "./CurrencyExposure.module.css";
import useDashboard from "../hooks/useDasboard";

function CurrencyExposure() {
  const { dashboard } = useDashboard();
  const holdings = dashboard?.holdings.content ?? [];

  const totals: Record<string, number> = {};
  let total = 0;

  // currency code -> display name
  const currencyNames: Record<string, string> = {
    SEK: "Svenska kronor",
    USD: "Amerikanska dollar",
  };

  for (const holding of holdings) {
    totals[holding.currency] =
      (totals[holding.currency] ?? 0) + holding.valueSek;
    total = total + holding.valueSek;
  }

  const currencyExposure = Object.entries(totals).map(([currency, value]) => ({
    currency,
    name: currencyNames[currency] ?? currency,
    value,
    percentage: Math.round((value / total) * 100),
  }));

  return (
    <AppCard
      sx={{
        marginTop: "10px",
      }}
    >
      <div className={styles.currencyHeader}>
        <h3>Valutaexponering</h3>
        <p>
          Visar hur portföljens totala värde är fördelat mellan olika valutor.
        </p>
      </div>

      <div className={styles.currencyBar}>
        {currencyExposure.map((item) => (
          <div
            key={item.currency}
            className={`${styles.currencySegment} ${
              styles[item.currency.toLowerCase()]
            }`}
            style={{ width: `${item.percentage}%` }}
          ></div>
        ))}
      </div>

      <ul className={styles.currencyList}>
        {currencyExposure.map((item) => (
          <li key={item.currency} className={styles.currencyRow}>
            <span
              className={`${styles.currencyDot} ${
                styles[item.currency.toLowerCase()]
              }`}
            />
            <span className={styles.currencyText}>
              <strong>{item.currency}</strong>
              <span>{item.name}</span>
            </span>
            <span className={styles.currencyPercentage}>
              {item.percentage} %
            </span>
            <span className={styles.currencyValue}>
              {item.value.toLocaleString("sv-SE")} kr
            </span>
          </li>
        ))}
      </ul>
    </AppCard>
  );
}

export default CurrencyExposure;
