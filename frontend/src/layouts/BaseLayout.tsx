import Card from "../components/Card";
import Sidebar from "../components/Sidebar";

const BaseLayout = () => {
  return (
    <div className="app-layout">
      {/* <Navbar /> */}
      <div className="app-body">
        {/* <Header/> */}
        <Sidebar />
        <main className="main-content">
          <Card>
            <h2>Card Title</h2>
            <p>Card Description</p>
          </Card>
        </main>
      </div>
      {/* <Footer /> */}
    </div>
  );
};
export default BaseLayout;
