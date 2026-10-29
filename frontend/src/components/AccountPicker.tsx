import styles from "./AccountPicker.module.css";
import { useAccounts } from "../hooks/useAccounts";
import Badge from "./Badge";

type AccountPickerProps = {
  selectedAccountId: number | null;
  onSelect: (accountId: number) => void;
};

const AccountPicker = ({ selectedAccountId, onSelect }: AccountPickerProps) => {
  const { accounts, loading, error } = useAccounts();

  if (loading) return <p>Laddar konton...</p>;
  if (error) return <p>{error}</p>;

  return (
    <section className={styles.container}>
      <h2>1. Välj konto</h2>
      <p>Vilket konto vill du rebalansera?</p>

      <div className={styles.accountList}>
        {accounts.map((account) => (
          <label
            key={account.id}
            className={`${styles.account} ${
              selectedAccountId === account.id ? styles.selected : ""
            }`}
          >
            <input
              type="radio"
              name="rebalanceAccount"
              checked={selectedAccountId === account.id}
              onChange={() => onSelect(account.id)}
            />

            <span>{account.accountName}</span>

            <Badge
              variant={
                account.accountType.toLowerCase() as
                  | "isk"
                  | "kf"
                  | "depa"
                  | "tjp"
              }
            >
              {account.accountType}
            </Badge>
          </label>
        ))}
      </div>
    </section>
  );
};

export default AccountPicker;
