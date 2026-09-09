import { Container } from "@mui/material";
import InnehavsForm from "../components/InnehavForm";
import InnehavsLista from "../components/Innehavslista";
import styles from "./InnehavPage.module.css";
import { holdings } from "../data/mockData";
import { useState } from "react";

function InnehavPage() {
  const [currentHoldings, setCurrentHoldings] = useState(holdings);

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
