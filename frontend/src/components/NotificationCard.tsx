import styles from "./NotificationCard.module.css";
import AppCard from "./AppCard";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faTriangleExclamation } from "@fortawesome/free-solid-svg-icons";
import type { LiveAlert } from "../types/notification";

type NotificationCardProps = {
  alerts: LiveAlert[];
};

const NotificationCard = ({ alerts }: NotificationCardProps) => {
  return (
    <AppCard>
      <p className={styles.title}>Notifikationer</p>
      <div className={styles.list}>
        {alerts.map((n, index) => (
          <div key={index} className={`${styles.card} ${styles.warning}`}>
            <FontAwesomeIcon
              icon={faTriangleExclamation}
              className={styles.icon}
            />
            <div className={styles.textWrapper}>
              <p>{n.message}</p>
              <p className={styles.time}>{n.created_at}</p>
            </div>
          </div>
        ))}
      </div>
    </AppCard>
  );
};

export default NotificationCard;
