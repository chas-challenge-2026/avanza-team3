import { Container } from "@mui/material";
import InnehavsForm from "../components/InnehavForm";
import InnehavsLista from "../components/Innehavslista";
import styles from "./InnehavPage.module.css";
import { holdings } from "../data/mockData";
import { useEffect, useState } from "react";
import { getHoldings, getPortfolio } from "../services/holdingService";

function InnehavPage() {
  const [currentHoldings, setCurrentHoldings] = useState(holdings);

  useEffect(() => {
    const testHolding= async () => {
      const holding = await getHoldings();
      console.log(holding);
    };
  
    testHolding();
  }, []);

  useEffect(() => {
    const testPortfolio = async () => {
      const portfolio = await getPortfolio();
      console.log(portfolio);
    };
  
    testPortfolio();
  }, []);

  const handleDelete = (id: number) => {
    setCurrentHoldings((current) =>
      current.filter((holding) => holding.id !== id)
    );

  };
  return (
    <Container className={styles.InnehavPageWrapper}>
      <h1>Innehav</h1>

      <InnehavsLista
        holdings={currentHoldings}
        onDelete={handleDelete}
        width="1200px"
        showDelete
      />

      <InnehavsForm />
    </Container>
  );
}

export default InnehavPage;
