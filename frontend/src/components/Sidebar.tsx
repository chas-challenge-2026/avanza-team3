import { NavLink } from "react-router-dom";
import styles from "./Sidebar.module.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faChartPie,
  faBriefcase,
  faRightLeft,
  faFileLines,
  faBell
} from "@fortawesome/free-solid-svg-icons";
import AppButton from "./Button";

function Sidebar() {
  return (
    <aside className={styles.sidebar}>
      <nav>
        <NavLink to="/">
          <FontAwesomeIcon icon={faChartPie} /> Överblick
        </NavLink>
        <NavLink to="/innehav">
          <FontAwesomeIcon icon={faBriefcase} /> Innehav
        </NavLink>
        <NavLink to="/trygghetsoversikt">
          <FontAwesomeIcon icon={faRightLeft} /> Trygghetsöversikt
        </NavLink>
        <NavLink to="/rapporter">
          <FontAwesomeIcon icon={faFileLines} /> Rapporter
        </NavLink>
        <NavLink to="/notiser">
          <FontAwesomeIcon icon={faBell} /> Notiser
        </NavLink>
      </nav>

      {/* <button className={styles.loginButton}>Logga in</button> */}
      <AppButton sx={{ marginTop: "auto" }} variant="contained">
        Logga In
      </AppButton>
    </aside>
  );
}

export default Sidebar;
