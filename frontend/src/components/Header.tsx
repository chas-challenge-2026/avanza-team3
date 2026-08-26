import styles from "./Header.module.css";
import useAuth from "../hooks/useAuth";
import { Button } from "@mui/material";

const Header = () => {
  const { user, logout }= useAuth();

  return (
    <header className={styles.header}>
      <h1>Avanza</h1>
      <div className={styles.actionWrapper}>
        <p>Inloggad som: {user?.name}</p>
        <Button variant="contained" onClick={logout}>
          Logga ut
        </Button>
      </div>
    </header>
  );
};
export default Header;
