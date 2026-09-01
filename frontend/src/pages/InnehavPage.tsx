import { Container } from "@mui/material";
import InnehavsForm from "../components/InnehavForm";
import InnehavsLista from "../components/Innehavslista";
import styles from "./InnehavPage.module.css";

function InnehavPage() {
  return (
    <Container className={styles.InnehavPageWrapper}>
      <h1>Innehav</h1>
      <Container className={styles.list}>
        <InnehavsLista />
      </Container>
      <Container className={styles.form}>
        <InnehavsForm />
      </Container>
    </Container>
  );
}

export default InnehavPage;
