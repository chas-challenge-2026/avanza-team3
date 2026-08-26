import styles from "./Badge.module.css";

type BadgeProps = {
  children: string;
  variant:
    | "over"
    | "under"
    | "ok"
    | "isk"
    | "kf"
    | "tjp"
    | "depa"
    | "aktie"
    | "drift";
};

const Badge = ({ children, variant }: BadgeProps) => {
  return (
    <span className={styles.badge + " " + styles[variant]}>{children}</span>
  );
};

export default Badge;
