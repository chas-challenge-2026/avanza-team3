import InnehavsLista from "../components/Innehavslista";
import { holdings } from "../data/mockData";

function PortfolioHealthPage() {
  return (
    <>
      <h1>Portföljhälsa</h1>
      <InnehavsLista holdings={holdings} width="1200px" detailed />
    </>
  );
}

export default PortfolioHealthPage;
