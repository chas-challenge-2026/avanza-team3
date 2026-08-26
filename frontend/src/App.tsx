import "./App.css";
import BaseLayout from "./layouts/BaseLayout";
import { Route, Routes } from "react-router-dom";

import PortfolioPage from "./pages/PortfolioPage";
import InnehavPage from "./pages/InnehavPage";
import TrygghetsoversiktPage from "./pages/TrygghetsoversiktPage";
import LoginPage from "./pages/LoginPage";

function App() {
  return (
    <>
      <Routes>

      {/* Sidor som inte använder BaseLayout */}
      <Route path="/login" element={<LoginPage />} />

      {/* Sidor som använder BaseLayout */}
        <Route element={<BaseLayout />}>
          <Route path="/" element={<PortfolioPage />} />
          <Route path="/innehav" element={<InnehavPage />} />
          <Route path="/trygghetsoversikt" element={<TrygghetsoversiktPage />} />
        </Route>
      </Routes>
    </>
  );
}

export default App;
