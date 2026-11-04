import styles from "./NotificationPage.module.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBell } from "@fortawesome/free-solid-svg-icons";
import { useMemo } from "react";
import useDashboard from "../hooks/useDasboard";
import { useAccountAllocations } from "../hooks/useAccountAllocations";
import { Link } from "react-router-dom";

const categoryLabels: Record<string, string> = {
  STOCK: "Aktier",
  FUND: "Fonder",
  BOND: "Räntor",
  OTHER: "Övrigt",
};

export default function NotificationPage() {
  const { dashboard } = useDashboard();

  const accounts = useMemo(
    () => dashboard?.accounts ?? [],
    [dashboard?.accounts],
  );

  const accountIds = accounts.map((account) => account.id);

  const { allocations, loading } = useAccountAllocations(accountIds);

  const notifications = allocations.flatMap((allocation) => {
    if (!allocation.targetsConfigured) return [];

    const account = accounts.find((item) => item.id === allocation.accountId);

    const deviations = allocation.categories
      .filter(
        (category) =>
          category.targetPct !== null &&
          Math.abs(category.actualPct - category.targetPct) > 5,
      )
      .sort(
        (a, b) =>
          Math.abs(b.actualPct - b.targetPct!) -
          Math.abs(a.actualPct - a.targetPct!),
      );

    const largest = deviations[0];

    if (!largest) return [];

    return [
      {
        id: allocation.accountId,
        accountType: account?.accountType ?? "Konto",
        category: categoryLabels[largest.category] ?? largest.category,
        actual: largest.actualPct,
        target: largest.targetPct!,
        difference: largest.actualPct - largest.targetPct!,
      },
    ];
  });

  const isLoading =
    !dashboard ||
    loading ||
    (accounts.length > 0 && allocations.length !== accounts.length);

  return (
    <div className={styles.container}>
      <div className={styles.titleRow}>
        <div className={styles.iconTitle}>
          <FontAwesomeIcon icon={faBell} className={styles.icon} />
          <h1 className={styles.title}>Notifikationer</h1>
        </div>
      </div>

      <h2 className={styles.sectionTitle}>Aktuella avvikelser</h2>

      {isLoading ? (
        <p className={styles.empty}>Laddar notifikationer...</p>
      ) : notifications.length === 0 ? (
        <p className={styles.empty}>Alla konton följer sina målallokeringar.</p>
      ) : (
        notifications.map((notification) => (
          <div key={notification.id} className={styles.card}>
            <span className={`${styles.iconWrapper} ${styles.warning}`}>
              <FontAwesomeIcon icon={faBell} />
            </span>

            <div className={styles.message}>
              <p className={styles.cardTitle}>
                {notification.accountType} – Avviker från målallokering
              </p>

              <p>
                Största avvikelsen: {notification.category},{" "}
                {notification.difference > 0 ? "+" : ""}
                {notification.difference.toFixed(1)} procentenheter.
              </p>

              <div>
                <span>
                  <span className={styles.bold}>Nuvarande:</span>{" "}
                  {notification.actual.toFixed(1)} %
                </span>{" "}
                <span>
                  <span className={styles.bold}>Mål:</span>{" "}
                  {notification.target.toFixed(1)} %
                </span>{" "}
                <span>
                  <span className={styles.bold}>Avvikelse:</span>{" "}
                  {notification.difference > 0 ? "+" : ""}
                  {notification.difference.toFixed(1)} pp
                </span>
              </div>
              <Link to="/rebalansering" className={styles.rebalanceLink}>
                Rebalansera →
              </Link>
            </div>
          </div>
        ))
      )}
    </div>
  );
}
