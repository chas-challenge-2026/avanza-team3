import styles from "./NotificationCard.module.css";

const NotificationCard = () => {
  return (
    <div className={styles.card}>
      <p className={styles.notification}>Notifikationer</p>
      <p>Din risknivå är 2% från mål</p>
    </div>
  );
};

export default NotificationCard;
