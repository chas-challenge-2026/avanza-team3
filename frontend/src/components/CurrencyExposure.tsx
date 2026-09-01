import AppCard from "./AppCard";
import styles from "./CurrencyExposure.module.css"

function CurrencyExposure(){

    const mockCurrencyExposure = [
        {
          currency: "SEK",
          name: "Svensk krona",
          percentage: 65,
          value: 160875,
        },
        {
          currency: "USD",
          name: "Amerikansk dollar",
          percentage: 25,
          value: 61875,
        },
        {
          currency: "EUR",
          name: "Euro",
          percentage: 10,
          value: 24750,
        },
      ];

      return (
        <AppCard sx={{
            marginTop: "10px"
            }}>
            <div className={styles.currencyHeader}>
                <h3>Valutaexponering</h3>
                <p>Visar hur portföljens totala värde är fördelat mellan olika valutor.</p>
            </div>

            <div className={styles.currencyBar}>
                {mockCurrencyExposure.map((item) => (
                <div
                    key={item.currency}
                    className={`${styles.currencySegment} ${
                    styles[item.currency.toLowerCase()]
                    }`}
                    style={{ width: `${item.percentage}%` }}
                >
                    <strong>{item.percentage}%</strong>
                    <span>{item.currency}</span>
                </div>
                ))}
            </div>  

            <table className={styles.currencyTable}>
  <thead>
    <tr>
      <th>Valuta</th>
      <th>Värde</th>
      <th>Procent av totala</th>
    </tr>
  </thead>

  <tbody>
    {mockCurrencyExposure.map((item) => (
      <tr key={item.currency}>
        <td>
          <div className={styles.currencyInfo}>
            <div className={`${styles.currencyIcon} ${styles[item.currency.toLowerCase()]}`}>
              {item.currency === "SEK" ? "kr" : item.currency === "USD" ? "$" : "€"}
            </div>

            <div className={styles.currencyText}>
              <strong>{item.currency}</strong>
              <span>{item.name}</span>
            </div>
          </div>
        </td>

        <td>
          {item.value.toLocaleString("sv-SE")} SEK
        </td>

        <td>
          <div className={styles.percentageCell}>
            <strong>{item.percentage}%</strong>

            <div className={styles.progressTrack}>
              <div
                className={`${styles.progress} ${styles[item.currency.toLowerCase()]}`}
                style={{ width: `${item.percentage}%` }}
              />
            </div>
          </div>
        </td>
      </tr>
    ))}
  </tbody>
</table>
        </AppCard>
      );

};

export default CurrencyExposure;