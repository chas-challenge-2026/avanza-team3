import styles from "./NotificationCard.module.css";
import { useState } from "react";
import AppCard from "./AppCard";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faTriangleExclamation } from "@fortawesome/free-solid-svg-icons";
import type { LiveAlert } from "../types/notification";
import type { AllocationRow } from "../types/dashboard";

type NotificationCardProps = {
  alerts: LiveAlert[];
  allocationRows: AllocationRow[];
};

const DriftCard = ({ row }: { row: AllocationRow }) => {
  const [open, setOpen] = useState(false);

  const difference = row.actual - row.target;

  return (
    <div
      className={`${styles.card} ${styles.warning}`}
      onClick={() => setOpen(!open)}
    >
      <FontAwesomeIcon icon={faTriangleExclamation} className={styles.icon} />

      <div className={styles.textWrapper}>
        <h3 className={styles.cardTitle}>
          {row.accountType} –{" "}
          {difference > 0 ? "Över målallokering" : "Under målallokering"}
        </h3>

        <p className={styles.cardInfo}>
          Din fördelning avviker från ditt angivna mål.
        </p>

        {open && (
          <div className={styles.values}>
            <div className={styles.valueItem}>
              <span className={styles.valueLabel}>Nuvarande</span>
              <p className={styles.valueNumber}>{row.actual.toFixed(1)} %</p>
            </div>

            <div className={styles.valueItem}>
              <span className={styles.valueLabel}>Mål</span>
              <p className={styles.valueNumber}>{row.target.toFixed(1)} %</p>
            </div>

            <div className={styles.valueItem}>
              <span className={styles.valueLabel}>Avvikelse</span>
              <p className={styles.valueNumber}>
                {difference > 0 ? "+" : ""}
                {difference.toFixed(1)} %
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

const NotificationCard = ({
  alerts,
  allocationRows,
}: NotificationCardProps) => {
  const driftRows = allocationRows.filter((row) => row.overThreshold);

  const otherAlerts = alerts.filter(
    (alert) =>
      alert.alert_type !== "DRIFT" && alert.alert_type !== "LIVE_DRIFT",
  );

  return (
    <AppCard>
      <p className={styles.title}>Notifikationer</p>

      <div className={styles.list}>
        {driftRows.map((row) => (
          <DriftCard key={row.accountType} row={row} />
        ))}
        {/* {driftRows.map((row) => {
          const difference = row.actual - row.target;

          return (
            <div
              key={row.accountType}
              className={`${styles.card} ${styles.warning}`}
            >
              <FontAwesomeIcon
                icon={faTriangleExclamation}
                className={styles.icon}
              />

              <div className={styles.textWrapper}>
                <strong>
                  {row.accountType} –{" "}
                  {difference > 0
                    ? "Över målallokering"
                    : "Under målallokering"}
                </strong>

                <p>Din fördelning avviker från ditt angivna mål.</p>

                <div className={styles.values}>
                  <div className={styles.valueItem}>
                    <span className={styles.valueLabel}>Nuvarande</span>
                    <strong className={styles.valueNumber}>
                      {row.actual.toFixed(1)} %
                    </strong>
                  </div>

                  <div className={styles.valueItem}>
                    <span className={styles.valueLabel}>Mål</span>
                    <strong className={styles.valueNumber}>
                      {row.target.toFixed(1)} %
                    </strong>
                  </div>

                  <div className={styles.valueItem}>
                    <span className={styles.valueLabel}>Avvikelse</span>
                    <strong className={styles.valueNumber}>
                      {difference > 0 ? "+" : ""}
                      {difference.toFixed(1)} %
                    </strong>
                  </div>
                </div>
              </div>
            </div>
          );
        })} */}

        {otherAlerts.map((alert, index) => (
          <div key={index} className={`${styles.card} ${styles.warning}`}>
            <FontAwesomeIcon
              icon={faTriangleExclamation}
              className={styles.icon}
            />

            <div className={styles.textWrapper}>
              <p>{alert.message}</p>
            </div>
          </div>
        ))}

        {driftRows.length === 0 && otherAlerts.length === 0 && (
          <p>Inga aktuella notifikationer.</p>
        )}
      </div>
    </AppCard>
  );
};

export default NotificationCard;
