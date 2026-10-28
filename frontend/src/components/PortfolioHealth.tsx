import { Gauge, gaugeClasses } from "@mui/x-charts/Gauge";
import { NavLink } from "react-router-dom";
import styles from "./PortfolioHealth.module.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faArrowRight } from "@fortawesome/free-solid-svg-icons";
import AppCard from "./AppCard";
import PopOver from "./Popover";
import { useState } from "react";
import InfoText from "./InfoText";

type PortfolioHealthProps = {
  value: number;
  riskScore: number;
  spread: number;
  help: {
    title: string;
    content: string;
  };
};

const PortfolioHealth = ({
  value,
  help,
  riskScore,
  spread,
}: PortfolioHealthProps) => {
  const [expanded, setExpanded] = useState(false);
  const valueStatus = () => {
    if (value < 40) {
      return "Låg";
    } else if (value > 39 && value < 70) {
      return "Måttlig";
    } else return "God";
  };

  const statusColors = {
    God: "var(--text-green)",
    Måttlig: "var(--text-warning)",
    Låg: "var(--text-error)",
  };

  return (
    <AppCard>
      <div className={styles.container}>
        <div className={styles.headerWrapper}>
          <h2>Portföljhälsa</h2>
          <PopOver title={help.title} content={help.content} />
        </div>

        <Gauge
          cornerRadius={"50%"}
          width={300}
          height={200}
          value={value}
          text={""}
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
          <text x="50%" y="64%" textAnchor="middle">
            <tspan
              x="50%"
              className={styles.gaugeStatus}
              style={{ fill: statusColors[valueStatus()] }}
            >
              {valueStatus()}
            </tspan>
            <tspan x="50%" dy="28" className={styles.gaugeScore}>
              {value}/100
            </tspan>
          </text>
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

        {expanded && (
          <div className={styles.details}>
            <div className={styles.metrics}>
              <div>
                <p>Riskpoäng</p>
                <strong>{riskScore}/100</strong>
              </div>

              <div>
                <p>Innehavsspridning</p>
                <strong>{spread}/100</strong>
              </div>
            </div>

            <p className={styles.explanation}>
              Portföljhälsan baseras till 50 % på risknivå och 50 % på
              innehavsspridning. Lägre risk och högre spridning ger högre poäng.
            </p>
            <NavLink to="/portfoljhalsa" className={styles.readMore}>
              Läs mer
              <FontAwesomeIcon icon={faArrowRight} />
            </NavLink>
          </div>
        )}
        {!expanded && (
          <button
            type="button"
            className={styles.expandButton}
            onClick={() => setExpanded(true)}
            aria-expanded={false}
          >
            Visa detaljer
          </button>
        )}
        <InfoText content={help.content} />
      </div>
    </AppCard>
  );
};

export default PortfolioHealth;
