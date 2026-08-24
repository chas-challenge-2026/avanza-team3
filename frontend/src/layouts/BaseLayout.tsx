import AppButton from "../components/AppButton";
import AppCard from "../components/AppCard";
import Sidebar from "../components/Sidebar";

const BaseLayout = () => {
  return (
    <div className="app-layout">
      {/* <Navbar /> */}
      <div className="app-body">
        {/* <Header/> */}
        <Sidebar />
        <main className="main-content">
          <AppCard variant="elevation">
            <h2>Card Title</h2>
            <p>Card Description</p>
            <AppButton variant="contained">Spara</AppButton>
          </AppCard>
        </main>
      </div>
      {/* <Footer /> */}
    </div>
  );
};
export default BaseLayout;
