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

const Sidebar = () => {
  return (
    <aside className={styles.sidebar}>
      <p className={styles.sidebarSection}>Meny</p>
      <nav>
        <NavLink to="/">
          <FontAwesomeIcon icon={faChartPie} /> Överblick
        </NavLink>
        <NavLink to="/innehav">
          <FontAwesomeIcon icon={faBriefcase} /> Innehav
        </NavLink>
        <NavLink to="/portfoljhalsa">
          <FontAwesomeIcon icon={faRightLeft} /> Portföljhälsa
        </NavLink>
        <NavLink to="/rapporter">
          <FontAwesomeIcon icon={faFileLines} /> Rapporter
        </NavLink>
        <NavLink to="/notiser">
          <FontAwesomeIcon icon={faBell} /> Notiser
        </NavLink>
      </nav>
    </aside>
  );
};

export default Sidebar;
