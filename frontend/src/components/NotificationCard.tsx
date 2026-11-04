import { useState } from "react";
import { Link } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faTriangleExclamation,
  faArrowRight,
} from "@fortawesome/free-solid-svg-icons";

import AppCard from "./AppCard";
import styles from "./NotificationCard.module.css";
import type { AccountAllocation } from "../hooks/useAccountAllocation";

type Account = {
  id: number;
  accountType: string;
};

type NotificationCardProps = {
  allocations: AccountAllocation[];
  accounts: Account[];
};

type Drift = {
  id: string;
  accountType: string;
  category: string;
  actual: number;
  target: number;
  difference: number;
};

const categoryLabels: Record<string, string> = {
  STOCK: "Aktier",
  FUND: "Fonder",
  BOND: "Räntor",
  OTHER: "Övrigt",
};

const DriftCard = ({ drift }: { drift: Drift }) => {
  const [open, setOpen] = useState(false);

  return (
    <div
      className={`${styles.card} ${styles.warning}`}
      onClick={() => setOpen(!open)}
    >
      <FontAwesomeIcon icon={faTriangleExclamation} className={styles.icon} />

      <div className={styles.textWrapper}>
        <h3 className={styles.cardTitle}>
          {drift.accountType} – Avviker från målallokering
        </h3>

        <p className={styles.cardInfo}>
          Största avvikelsen: {drift.category},{" "}
          {drift.difference > 0 ? "+" : ""}
          {drift.difference.toFixed(1)} procentenheter.
        </p>

        {open && (
          <div className={styles.values}>
            <div className={styles.valueItem}>
              <span className={styles.valueLabel}>Nuvarande</span>
              <p className={styles.valueNumber}>{drift.actual.toFixed(1)} %</p>
            </div>

            <div className={styles.valueItem}>
              <span className={styles.valueLabel}>Mål</span>
              <p className={styles.valueNumber}>{drift.target.toFixed(1)} %</p>
            </div>

            <div className={styles.valueItem}>
              <span className={styles.valueLabel}>Avvikelse</span>
              <p className={styles.valueNumber}>
                {drift.difference > 0 ? "+" : ""}
                {drift.difference.toFixed(1)} pp
              </p>
            </div>

            <Link
              to={`/rebalansering?account=${drift.id}`}
              className={styles.viewMore}
              onClick={(event) => event.stopPropagation()}
            >
              Rebalansera
              <FontAwesomeIcon icon={faArrowRight} />
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};

const NotificationCard = ({ allocations, accounts }: NotificationCardProps) => {
  const drifts: Drift[] = allocations.flatMap((allocation) => {
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
        id: String(allocation.accountId),
        accountType: account?.accountType ?? "Konto",
        category: categoryLabels[largest.category] ?? largest.category,
        actual: largest.actualPct,
        target: largest.targetPct!,
        difference: largest.actualPct - largest.targetPct!,
      },
    ];
  });

  return (
    <AppCard>
      <p className={styles.title}>Notifikationer</p>

      <div className={styles.list}>
        {drifts.map((drift) => (
          <DriftCard key={drift.id} drift={drift} />
        ))}

        {drifts.length === 0 && <p>Inga aktuella notifikationer.</p>}
      </div>
    </AppCard>
  );
};

export default NotificationCard;
