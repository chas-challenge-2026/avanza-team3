import InnehavsForm from "../components/InnehavForm";
import InnehavsLista from "../components/Innehavslista";
import styles from "./InnehavPage.module.css";

function InnehavPage() {
  return (
    <div className={styles.InnehavPageWrapper}>
      <h1>Innehav</h1>
      <InnehavsLista />

      <InnehavsForm />
    </div>
  );
}

export default InnehavPage;
