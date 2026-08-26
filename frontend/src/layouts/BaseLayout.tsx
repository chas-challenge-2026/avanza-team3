import AppCard from "../components/AppCard";
import Header from "../components/Header";
import Sidebar from "../components/Sidebar";
import DataTable from "../components/DataTable";
import styles from "./BaseLayout.module.css";
import { Outlet } from "react-router-dom";
import Badge from "../components/Badge";

const BaseLayout = () => {
  return (
    <div className={styles.appLayout}>
      <Sidebar />
      <div className={styles.appBody}>
        <Header/>
        <main className={styles.mainContent}>
          <Outlet />

          {/* För att testa / se listkomponent. Tas bort senare */}
          {/* <DataTable
            width="600px"
            title="Konton"
            rows={[
              {
                id: 1,
                account: "Anna ISK",
                type: "ISK",
                label: "ISK",
                value: "312 340",
                share: "43.8%"
              },
              {
                id: 2,
                account: "Anna KF",
                type: "KF",
                label: "KF",
                value: "201 230",
                share: "28.3%"
              },
              {
                id: 3,
                account: "Tjänstepension",
                type: "TJP",
                label: "TJP",
                value: "145 560",
                share: "20.4%"
              },
              {
                id: 4,
                account: "Depå",
                type: "Depa",
                label: "Depå",
                value: "53 437",
                share: "7,5%"
              }
            ]}
            columns={[
              { field: "account", headerName: "Konto" },
              { field: "type", headerName: "Typ", isBadge: true },
              { field: "value", headerName: "Värde" },
              { field: "share", headerName: "Andel" }
            ]}
          /> */}
        </main>
      </div>
    </div>
  );
};

export default BaseLayout;
