import { Gauge, gaugeClasses } from "@mui/x-charts/Gauge";
import { NavLink } from "react-router-dom";
import styles from "./PortfolioHealth.module.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faArrowRight } from "@fortawesome/free-solid-svg-icons";
import AppCard from "./AppCard";

type PortfolioHealthProps = {
  value: number;
};

const PortfolioHealth = ({ value }: PortfolioHealthProps) => {
  const valueStatus = () => {
    if (value < 40) {
      return "Dålig";
    } else if (value > 39 && value < 70) {
      return "Okej";
    } else return "God";
  };

  const statusColors = {
    God: "var(--text-green)",
    Okej: "var(--text-warning)",
    Dålig: "var(--text-error)",
  };

  return (
    <AppCard>
      <div className={styles.container}>
        <h2>Portföljhälsa</h2>
        <Gauge
          cornerRadius={"50%"}
          width={300}
          height={200}
          value={value}
          text={valueStatus()}
          startAngle={-90}
          endAngle={90}
          sx={{
            [`& .${gaugeClasses.valueArc}`]: {
              fill: "url(#gaugeGradient)",
            },
            [`& .${gaugeClasses.valueText} text`]: {
              fontSize: 25,
              fontWeight: 500,
              fill: statusColors[valueStatus()],
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
                stopColor={statusColors[valueStatus()]}
                stopOpacity={0.3}
              />
              <stop
                offset="100%"
                stopColor={statusColors[valueStatus()]}
                stopOpacity={1}
              />
            </linearGradient>
          </defs>
        </Gauge>
        <NavLink to="/portfoljhalsa">
          Läs mer{" "}
          <FontAwesomeIcon className={styles.arrowIcon} icon={faArrowRight} />
        </NavLink>
      </div>
    </AppCard>
  );
};

export default PortfolioHealth;
