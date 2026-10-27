import type { Instrument } from "../types/Instrument";
import AppCard from "./AppCard";
import styles from "./ApprovedHoldingsList.module.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faTrashAlt } from "@fortawesome/free-solid-svg-icons";

type Props = {
  approved: Instrument[];
  onRemove: (id: number) => void;
};

const ApprovedHoldingsList = ({ approved, onRemove }: Props) => {
  return (
    <div className={styles.wrapper}>
      <AppCard>
        <h2 className={styles.sectionTitle}>Godkända innehav</h2>
        <p className={styles.label}>
          Dessa instrument får användas vid rebalansering.
        </p>
        <ul className={styles.list}>
          {approved.map((i) => (
            <li key={i.id} className={styles.row}>
              <span>
                <strong>{i.ticker}</strong> - {i.name}
              </span>
              <button
                type="button"
                className={styles.removeButton}
                onClick={() => onRemove(i.id)}
                aria-label={`Ta bort ${i.name}`}
              >
                <FontAwesomeIcon icon={faTrashAlt} />
              </button>
            </li>
          ))}
        </ul>
      </AppCard>
    </div>
  );
};

export default ApprovedHoldingsList;
