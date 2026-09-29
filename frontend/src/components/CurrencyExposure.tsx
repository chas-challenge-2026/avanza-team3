import AppCard from "./AppCard";
import styles from "./CurrencyExposure.module.css"

function CurrencyExposure(){

    const mockCurrencyExposure = [
        {
          currency: "SEK",
          name: "Svenska kronor",
          percentage: 48,
          value: 342032,
        },
        {
          currency: "USD",
          name: "Amerikanska dollar",
          percentage: 31,
          value: 220896,
        },
        {
          currency: "EUR",
          name: "Euro",
          percentage: 13,
          value: 92634,
        },
        {
          currency: "GBP",
          name: "Brittiska pund",
          percentage: 5,
          value: 35628,
        },
        {
          currency: "JPY",
          name: "Japanska yen",
          percentage: 3,
          value: 21377,
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

            <div className={styles.currencyGrid}>
              {mockCurrencyExposure.map((item) => (
                <div key={item.currency} className={styles.currencyCard}>
                  <div className={styles.currencyCardTop}>
                    <div
                      className={`${styles.currencyIcon} ${
                        styles[item.currency.toLowerCase()]
                      }`}
                    >
                      {item.currency.slice(0, 2)}
                    </div>
                    <span className={styles.currencyPercentage}>
                      {item.percentage}%
                    </span>
                  </div>

                  <div className={styles.currencyText}>
                    <strong>{item.currency}</strong>
                    <span>{item.name}</span>
                  </div>

                  <p className={styles.currencyValue}>
                    {item.value.toLocaleString("sv-SE")} kr
                  </p>
                </div>
              ))}
            </div>
        </AppCard>
      );

};

export default CurrencyExposure;