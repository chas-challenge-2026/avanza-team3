import { Container } from "@mui/material";
import InnehavsForm from "../components/InnehavForm";
import InnehavsLista from "../components/Innehavslista";
import styles from "./InnehavPage.module.css";
import { useHoldings } from "../hooks/useHoldings";

function InnehavPage() {
  const {
    holdings,
    loading,
    error,
    // removeHolding,
  } = useHoldings();

  if (loading) return <p>Laddar...</p>;
  if (error) return <p>{error}</p>;

  return (
    <Container className={styles.InnehavPageWrapper}>
      <h1>Innehav</h1>
      <InnehavsLista
        holdings={holdings}
        columns={[
          "ticker",
          "instrumentName",
          "quantity",
          "avgBuyPrice",
          "account_type",
        ]}
        // onDelete={removeHolding}
        // width="1200px"
        // showDelete
      />
      <InnehavsForm />
      console.log(holdings);
    </Container>
  );
}

export default InnehavPage;
