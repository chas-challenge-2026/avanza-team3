import styles from "./NotificationCard.module.css";
import AppCard from "./AppCard";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faCircleCheck,
  faTriangleExclamation,
  faCircleInfo,
} from "@fortawesome/free-solid-svg-icons";

const mockNotifications = [
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
];

const NotificationCard = () => {
  return (
    <AppCard>
      <p className={styles.title}>Notifikationer</p>
      <div className={styles.list}>
        {mockNotifications.map((n) => (
          <div key={n.id} className={`${styles.card} ${styles[n.type]}`}>
            <FontAwesomeIcon icon={n.icon} className={styles.icon} />
            <div className={styles.textWrapper}>
              <p className={styles.notificationTitle}>{n.title}</p>
              <p className={styles.notification}>{n.message}</p>
              <p className={styles.time}>{n.time}</p>
            </div>
          </div>
        ))}
      </div>
    </AppCard>
  );
};

export default NotificationCard;
