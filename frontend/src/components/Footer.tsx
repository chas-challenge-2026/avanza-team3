import { NavLink } from "react-router-dom";
import styles from "./Footer.module.css";

const Footer = () => {
  return (
    <footer className={styles.footer}>
      <div className={styles.top}>
        <div className={styles.logo}>
          <span className={styles.logoBadge}>A</span>
          Avanza
        </div>
        <nav aria-label="Sidfot" className={styles.links}>
          <NavLink to="/">Överblick</NavLink>
          <NavLink to="/rebalansering">Rebalansering</NavLink>
          <NavLink to="/portfoljhalsa">Portföljhälsa</NavLink>
          <NavLink to="/notiser">Notiser</NavLink>
        </nav>
      </div>
      <div className={styles.bottom}>
        <span>© 2026</span>
      </div>
    </footer>
  );
};

export default Footer;
