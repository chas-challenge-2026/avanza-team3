import "./App.css";
import Card from "./components/Card";
import BaseLayout from "./layouts/BaseLayout";

function App() {
  return (
    <>
      <BaseLayout />
      <Card>
        <h2>Card Title</h2>
        <p>Content</p>
      </Card>
    </>
  );
}

export default App;
