import styles from "./NotificationCard.module.css";
import AppCard from "./AppCard";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faTriangleExclamation } from "@fortawesome/free-solid-svg-icons";
import type { Alert } from "../types/dashboard";

type NotificationCardProps = {
  alerts: Alert[];
};

const NotificationCard = ({ alerts }: NotificationCardProps) => {
  return (
    <AppCard>
      <p className={styles.title}>Notifikationer</p>
      <div className={styles.list}>
        {alerts.map((n) => (
          <div key={n.id} className={`${styles.card} ${styles.warning}`}>
            <FontAwesomeIcon
              icon={faTriangleExclamation}
              className={styles.icon}
            />
            <div className={styles.textWrapper}>
              <p>{n.message}</p>
              <p className={styles.time}>
                {new Date(n.createdAt).toLocaleDateString("sv-SE", {
                  day: "2-digit",
                  month: "2-digit",
                  hour: "2-digit",
                  minute: "2-digit",
                })}
              </p>
            </div>
          </div>
        ))}
      </div>
    </AppCard>
  );
};

export default NotificationCard;
