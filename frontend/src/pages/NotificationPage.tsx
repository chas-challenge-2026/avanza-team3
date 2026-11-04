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

const formatPct = (value: number) =>
  value.toLocaleString("sv-SE", {
    minimumFractionDigits: 1,
    maximumFractionDigits: 1,
  });

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
                {notification.accountType === "Depa"
                  ? "Depå"
                  : notification.accountType}{" "}
                – Avviker från målallokering
              </p>
              <p className={styles.description}>
                Största avvikelsen: {notification.category}
              </p>

              <div className={styles.stats}>
                <div className={styles.stat}>
                  <span className={styles.statLabel}>Nuvarande</span>
                  <span className={styles.statValue}>
                    {formatPct(notification.actual)} %
                  </span>
                </div>
                <div className={styles.stat}>
                  <span className={styles.statLabel}>Mål</span>
                  <span className={styles.statValue}>
                    {formatPct(notification.target)} %
                  </span>
                </div>
                <div className={styles.stat}>
                  <span className={styles.statLabel}>Avvikelse</span>
                  <span className={`${styles.statValue} ${styles.deviation}`}>
                    {notification.difference > 0 ? "+" : ""}
                    {formatPct(notification.difference)} %
                  </span>
                </div>
              </div>
            </div>

            <Link to="/rebalansering" className={styles.rebalanceLink}>
              Rebalansera →
            </Link>
          </div>
        ))
      )}
    </div>
  );
}
