import styles from "./Header.module.css";
import useAuth from "../hooks/useAuth";
import { Button } from "@mui/material";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faRightFromBracket } from "@fortawesome/free-solid-svg-icons";

const getInitials = (name?: string) => {
  if (!name) return "";
  return name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("");
};

const Header = () => {
  const { user, logout } = useAuth();

  return (
    <header className={styles.header}>
      <div className={styles.brand}>
        <span className={styles.logoMark}>A</span>
        <h1>Avanza</h1>
      </div>
      <div className={styles.actionWrapper}>
        <div className={styles.userInfo}>
          <span className={styles.avatar}>{getInitials(user?.name)}</span>
          <p className={styles.userName}>{user?.name}</p>
        </div>
        <Button
          className={styles.logoutButton}
          variant="outlined"
          onClick={logout}
        >
          <FontAwesomeIcon icon={faRightFromBracket} />
          Logga ut
        </Button>
      </div>
    </header>
  );
};
export default Header;
