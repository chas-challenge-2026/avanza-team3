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
  const isOutline = variant === "over" || variant === "under";
  console.log(isOutline, styles["badge-outline"]);
  return (
    <span
      className={
        styles.badge +
        " " +
        styles[variant] +
        " " +
        (isOutline ? styles["badge-outline"] : "")
      }
    >
      {children}
    </span>
  );
};

export default Badge;
