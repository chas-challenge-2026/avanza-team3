import Header from "../components/Header";
import Sidebar from "../components/Sidebar";
import styles from "./BaseLayout.module.css";
import { Outlet } from "react-router-dom";

const BaseLayout = () => {
  return (
    <div className={styles.appLayout}>
      <Header />
      <div className={styles.appBody}>
        <Sidebar />
        <main className={styles.mainContent}>
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default BaseLayout;
