import { Gauge, gaugeClasses } from "@mui/x-charts/Gauge";
import { NavLink } from "react-router-dom";
import styles from "./PortfolioHealth.module.css";

type PortfolioHealthProps = {
  value: number;
  status: "God" | "Okej" | "Dålig";
};

const PortfolioHealth = ({ value, status }: PortfolioHealthProps) => {
  const statusColors = {
    God: "var(--text-green)",
    Okej: "var(--text-warning)",
    Dålig: "var(--text-error)",
  };

  return (
    <>
      <div className={styles.container}>
        <h3>Portföljhälsa</h3>
        <Gauge
          width={300}
          height={200}
          value={value}
          text={status}
          startAngle={-90}
          endAngle={90}
          sx={{
            [`& .${gaugeClasses.valueArc}`]: {
              fill: statusColors[status],
            },
            [`& .${gaugeClasses.valueText}`]: {
              transform: "translateY(-40px)",
            },
          }}
        />
        <NavLink to="/portfoljhalsa"> Klicka för att läsa mer</NavLink>
      </div>
    </>
  );
};

export default PortfolioHealth;
