import styles from "./NotificationCard.module.css";
import AppCard from "./AppCard";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faCircleCheck,
  faTriangleExclamation,
  faCircleInfo,
} from "@fortawesome/free-solid-svg-icons";
import type { Alert } from "../types/dashboard";

/* const mockNotifications = [
  {
    id: 1,
    type: "success",
    icon: faCircleCheck,
    title: "Utdelning mottagen",
    message: "Avanza: 1 240 kr från Investor B.",
    time: "Idag 09:14",
  },
  {
    id: 2,
    type: "warning",
    icon: faTriangleExclamation,
    title: "Hög exponering",
    message: "Din risknivå är 2% från mål.",
    time: "Igår 17:30",
  },
  {
    id: 3,
    type: "info",
    icon: faCircleInfo,
    title: "Årsrapport tillgänglig",
    message: "Ericsson AB publicerade sin Q3-rapport.",
    time: "Mån 11:00",
  },
]; */

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
              <p className={styles.time}>{n.createdAt}</p>
            </div>
          </div>
        ))}
      </div>
    </AppCard>
  );
};

export default NotificationCard;
