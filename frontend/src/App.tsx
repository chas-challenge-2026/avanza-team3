import "./App.css";
import Sidebar from "./components/Sidebar";
import Card from "./components/Card";
import BaseLayout from "./layouts/BaseLayout";

function App() {
  return (
    <>
      <Sidebar />
      <BaseLayout />
      <Card>
        <h2>Card Title</h2>
        <p>Content</p>
      </Card>
    </>
  );
}

export default App;
