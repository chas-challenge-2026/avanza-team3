import { useState, useEffect } from "react";
import styles from "./NotificationPage.module.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBell, faTrashAlt } from "@fortawesome/free-solid-svg-icons";

type Notification = {
  id: number;
  alertType: string;
  message: string;
  dismissed: boolean;
  createdAt: string;
};

type LiveAlert = {
  alert_type: string;
  message: string;
  dismissed: boolean;
  created_at: string;
};

export default function NotificationPage() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [liveAlerts, setLiveAlerts] = useState<LiveAlert[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const token = localStorage.getItem("token");

  function dismissLiveAlert(index: number) {
    setLiveAlerts((prev) => prev.filter((_, i) => i !== index));
  }

  function dismissNotification(id: number) {
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, dismissed: true } : n)),
    );
  }

  useEffect(() => {
    async function getAlerts() {
      try {
        const [alertsRes, liveAlertsRes] = await Promise.all([
          fetch("/api/alerts", {
            headers: { Authorization: `Bearer ${token}` },
          }),
          fetch("/api/alerts/live", {
            headers: { Authorization: `Bearer ${token}` },
          }),
        ]);

        if (!alertsRes.ok || !liveAlertsRes.ok) {
          throw new Error("Kunde inte hämta notifikationer");
        }

        const alertsData = await alertsRes.json();
        const liveAlertsData = await liveAlertsRes.json();

        setNotifications(alertsData.content);
        setLiveAlerts(liveAlertsData);
      } catch (err) {
        setError("Kunde inte hämta notifikationer");
      } finally {
        setLoading(false);
      }
    }

    getAlerts();
  }, [token]);

  if (loading) return <p>Laddar notifikationer...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Notifikationer</h1>

      <h2 className={styles.sectionTitle}>Live-varningar</h2>
      {liveAlerts.length === 0 && (
        <p className={styles.empty}>Inga live-varningar</p>
      )}
      {liveAlerts.map((a, index) => (
        <div key={index} className={styles.card}>
          <span className={styles.iconWrapper}>
            <FontAwesomeIcon icon={faBell} />
          </span>
          <p className={styles.message}>{a.message}</p>
          <span className={styles.time}>{a.created_at}</span>
          <span onClick={() => dismissLiveAlert(index)}>
            <FontAwesomeIcon icon={faTrashAlt} />
          </span>
        </div>
      ))}

      <h2 className={styles.sectionTitle}>Äldre notifikationer</h2>
      {notifications.length === 0 && (
        <p className={styles.empty}>Inga sparade notifikationer</p>
      )}
      {notifications
        .filter((n) => !n.dismissed)
        .map((n) => (
          <div key={n.id} className={styles.card}>
            <span className={styles.iconWrapper}>
              <FontAwesomeIcon icon={faBell} />
            </span>
            <p className={styles.message}>{n.message}</p>
            <span className={styles.time}>
              {new Date(n.createdAt).toLocaleDateString("sv-SE", {
                day: "2-digit",
                month: "2-digit",
                hour: "2-digit",
                minute: "2-digit",
              })}{" "}
            </span>
            <span onClick={() => dismissNotification(n.id)}>
              <FontAwesomeIcon icon={faTrashAlt} />
            </span>
          </div>
        ))}
    </div>
  );
}
