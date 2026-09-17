import { useState, useEffect } from "react";
import styles from "./NotificationPage.module.css";

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

        setNotifications(alertsData);
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
    <div>
      <h1>Notifikationer</h1>

      <h2>Live-varningar</h2>
      {liveAlerts.length === 0 && <p>Inga live-varningar</p>}
      {liveAlerts.map((a, index) => (
        <p key={index}>
          {a.message} ({a.created_at})
        </p>
      ))}

      <h2>Sparade notifikationer</h2>
      {notifications.length === 0 && <p>Inga sparade notifikationer</p>}
      {notifications.map((n) => (
        <p key={n.id}>
          {n.message} ({n.createdAt})
        </p>
      ))}
    </div>
  );
}
