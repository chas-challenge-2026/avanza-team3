import styles from "./Header.module.css";

type HeaderProps = {
  userName: string;
};

const Header = ({ userName }: HeaderProps) => {
  return (
    <header className={styles.header}>
      <h1>Avanza</h1>
      <div className={styles.actionWrapper}>
        <p>Inloggad som: {userName}</p>
      </div>
    </header>
  );
};
export default Header;
