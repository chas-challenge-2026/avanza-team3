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
          cornerRadius={"50%"}
          width={300}
          height={200}
          value={value}
          text={status}
          startAngle={-90}
          endAngle={90}
          sx={{
            [`& .${gaugeClasses.valueArc}`]: {
              fill: "url(#gaugeGradient)",
            },
            [`& .${gaugeClasses.valueText}`]: {
              fontSize: 25,
              fontWeight: 500,
              transform: "translateY(-40px)",
            },
          }}
        >
          <defs>
            <linearGradient
              id="gaugeGradient"
              x1="0%"
              y1="0%"
              x2="100%"
              y2="0%"
            >
              <stop
                offset="0%"
                stopColor={statusColors[status]}
                stopOpacity={0.3}
              />
              <stop
                offset="100%"
                stopColor={statusColors[status]}
                stopOpacity={1}
              />
            </linearGradient>
          </defs>
        </Gauge>
        <NavLink to="/portfoljhalsa"> Klicka för att läsa mer</NavLink>
      </div>
    </>
  );
};

export default PortfolioHealth;
