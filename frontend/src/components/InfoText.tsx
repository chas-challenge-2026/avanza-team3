import styles from "./InfoText.module.css";

type Props = {
  content: string;
};

const InfoText = ({ content }: Props) => {
  return <p className={styles.infoText}>{content}</p>;
};

export default InfoText;
