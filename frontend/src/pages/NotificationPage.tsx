import { useState, useEffect } from "react";
import styles from "./NotificationPage.module.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBell, faTrashAlt } from "@fortawesome/free-solid-svg-icons";
import type { Notification } from "../types/notification";
import type { AllocationRow } from "../types/dashboard";
import useDashboard from "../hooks/useDasboard";

type LiveAlertCardProps = {
  row: AllocationRow;
  onDismiss: () => void;
};

const LiveAlertCard = ({ row, onDismiss }: LiveAlertCardProps) => {
  const difference = row.actual - row.target;

  return (
    <div className={`${styles.card} ${styles["border-left"]}`}>
      <span className={styles.iconWrapper}>
        <FontAwesomeIcon icon={faBell} />
      </span>

      <div className={styles.message}>
        <strong>
          {row.accountType} –{" "}
          {difference > 0 ? "Över målallokering" : "Under målallokering"}
        </strong>
        <p>Din fördelning avviker från ditt angivna mål.</p>

        <div>
          <span>Nuvarande: {row.actual.toFixed(1)} %</span>{" "}
          <span>Mål: {row.target.toFixed(1)} %</span>{" "}
          <span>
            Avvikelse: {difference > 0 ? "+" : ""}
            {difference.toFixed(1)} %
          </span>
        </div>
      </div>

      <span onClick={onDismiss}>
        <FontAwesomeIcon icon={faTrashAlt} className={styles.trashcan} />
      </span>
    </div>
  );
};

export default function NotificationPage() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dismissedRows, setDismissedRows] = useState<string[]>([]);
  const { dashboard } = useDashboard();
  const driftRows = (dashboard?.allocationRows ?? []).filter(
    (row) => row.overThreshold && !dismissedRows.includes(row.accountType),
  );

  const token = localStorage.getItem("token");

  function dismissNotification(id: number) {
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, dismissed: true } : n)),
    );
  }

  useEffect(() => {
    async function getAlerts() {
      try {
        const alertsRes = await fetch("/api/alerts", {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!alertsRes.ok) {
          throw new Error("Kunde inte hämta notifikationer");
        }

        const alertsData = await alertsRes.json();
        setNotifications(alertsData.content);
      } catch {
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
      <div className={styles.titleRow}>
        <div className={styles.iconTitle}>
          <FontAwesomeIcon icon={faBell} className={styles.icon} />
          <h1 className={styles.title}>Notifikationer</h1>
        </div>
      </div>

      <h2 className={styles.sectionTitle}>Live-varningar</h2>
      {driftRows.length === 0 && (
        <p className={styles.empty}>Inga live-varningar</p>
      )}
      {driftRows.map((row) => (
        <LiveAlertCard
          key={row.accountType}
          row={row}
          onDismiss={() =>
            setDismissedRows([...dismissedRows, row.accountType])
          }
        />
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
            <span
              onClick={() => dismissNotification(n.id)}
              className={styles.trashcan}
            >
              <FontAwesomeIcon icon={faTrashAlt} />
            </span>
          </div>
        ))}
    </div>
  );
}
