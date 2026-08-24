import { NavLink } from "react-router-dom";
import styles from "./Sidebar.module.css";

function Sidebar() {
  return (
    <aside className={styles.sidebar}>
      <nav>
        <NavLink to="/">Överblick</NavLink>
        <NavLink to="/innehav">Innehav</NavLink>
        <NavLink to="/transaktioner">Transaktioner</NavLink>
        <NavLink to="/rapporter">Rapporter</NavLink>
        <NavLink to="/notiser">Notiser</NavLink>
      </nav>

      <button className={styles.loginButton}>Logga in</button>
    </aside>
  );
}

export default Sidebar;
