import { Container } from "@mui/material";
import InnehavsForm from "../components/InnehavForm";
import InnehavsLista from "../components/Innehavslista";
import styles from "./InnehavPage.module.css";

function InnehavPage() {
  return (
    <Container className={styles.InnehavPageWrapper}>
      <h1>Innehav</h1>

      <InnehavsLista width="1200px" />

      <InnehavsForm />
    </Container>
  );
}

export default InnehavPage;
