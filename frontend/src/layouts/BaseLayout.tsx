import Sidebar from "../components/Sidebar";
import { Outlet } from "react-router-dom";

const BaseLayout = () => {
  return (
    <div className="app-layout">
      {/* <Navbar /> */}
      <div className="app-body">
        {/* <Header/> */}
        <Sidebar />
        <main className="main-content">
          <Outlet/>
        </main>
      </div>
      {/* <Footer /> */}
    </div>
  );
};
export default BaseLayout;
