import AppCard from "../components/AppCard";
import styles from "./PortfolioHealthPage.module.css";
import { Gauge } from "@mui/x-charts/Gauge";
// import usePortfolioAllocations from "../hooks/usePortfolioAllocation";
import DonutChart from "../components/DonutChart";
import useDashboard from "../hooks/useDasboard";
import InnehavsLista from "../components/Innehavslista";
import { useHoldings } from "../hooks/useHoldings";
import AllocationHandler from "../components/AllocationHandler";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faStethoscope } from "@fortawesome/free-solid-svg-icons";
import PopOver from "../components/Popover";

function PortfolioHealthPage() {
  // const { rows } = usePortfolioAllocations();
  const { dashboard, loading } = useDashboard();
  const { holdings } = useHoldings();

  const statusColors = {
    good: "#16a34a",
    warning: "#d97706",
    danger: "#dc2626"
  };

  // const accountColors: Record<string, string> = {
  //   ISK: "#16a34a",
  //   KF: "#1976d2",
  //   Depa: "#8b69a7",
  // };

  const changePercent = 5;

  const trendStatus = () => {
    return changePercent >= 0 ? "good" : "danger";
  };

  const riskPoint = 42;

  const riskLevel = () => {
    if (riskPoint < 30) {
      return "good";
    } else if (riskPoint <= 60) {
      return "warning";
    } else {
      return "danger";
    }
  };

  const riskLevelText = {
    good: "Låg Risk",
    warning: "Måttlig Risk",
    danger: "Hög Risk"
  };

  const diversification = 72;

  const diversificationLevel = () => {
    if (diversification >= 70) {
      return "good";
    } else if (diversification >= 40) {
      return "warning";
    } else {
      return "danger";
    }
  };

  const diversificationText = {
    good: "God spridning",
    warning: "Måttlig spridning",
    danger: "Låg spridning"
  };

  // const allocationData = rows.map((row) => ({
  //   label: row.accountType,
  //   value: row.actual,
  //   color: accountColors[row.accountType],
  // }));

  // const allocations =
  //   dashboard?.allocationRows.map((row, index) => ({
  //     id: `${row.accountType}-${index}`,
  //     label: row.accountType,
  //     value: row.target
  //   })) ?? [];

  const mockAllocations = [
    { id: "stocks", label: "Aktier", value: 50, target: 60 },
    { id: "funds", label: "Fonder", value: 30, target: 50 },
    { id: "bonds", label: "Ränteplaceringar", value: 20, target: 10 }
  ];

  return (
    <>
      <div className={styles.container}>
        <div className={styles.titleRow}>
          <div className={styles.iconTitle}>
            <FontAwesomeIcon icon={faStethoscope} className={styles.icon} />
            <div className={styles.titleText}>
              <h1 className={styles.title}>Min Portföljhälsa</h1>
            </div>
          </div>
        </div>
      </div>
      <div className={styles.container}>
        <AppCard>
          <div className={styles.kpiCard + " " + styles[riskLevel()]}>
            <div className={styles.header}>
              <p className={styles.label}>Riskpoäng</p>
              <PopOver
                title="Vad betyder Riskpoäng?"
                content="Riskpoäng sammanfattar hur mycket värdet på en investering kan svänga och hur stor risken är för förlust. Ju högre poäng, desto större svängningar kan du behöva tåla."
              />
            </div>
            <div className={styles.row}>
              <div>
                <p className={styles.value}>{riskPoint}/100</p>
                <p className={styles.label + " " + styles[riskLevel()]}>
                  {riskLevelText[riskLevel()]}
                </p>
              </div>
              <Gauge
                sx={{
                  "& .MuiGauge-valueArc": { fill: statusColors[riskLevel()] },
                  "& .MuiGauge-valueText": { display: "none" }
                }}
                width={90}
                height={90}
                innerRadius="40%"
                outerRadius="80%"
                value={riskPoint}
              />
            </div>
          </div>
        </AppCard>
        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Totalt Portföljvärde</p>
            </div>
            <p className={styles.value}>
              {dashboard?.totalPortfolioValue.toLocaleString("sv-se", {
                maximumFractionDigits: 0
              })}{" "}
              SEK
            </p>
            <p className={styles.label + " " + styles[trendStatus()]}>
              {changePercent >= 0 ? "+" : ""}
              {changePercent}% i år
            </p>
          </div>
        </AppCard>

        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Antal innehav</p>
            </div>
            <p className={styles.value}>
              {dashboard?.holdings.totalElements} st
            </p>
          </div>
        </AppCard>
        <AppCard>
          <div className={styles.kpiCard}>
            <div className={styles.header}>
              <p className={styles.label}>Diversifieringsgrad</p>
              <PopOver
                title="Vad betyder Diversifieringsgrad?"
                content="Diversifieringsgrad visar hur spridd en portfölj är.
              Bra spridning: investeringar i många bolag och branscher."
              />
            </div>
            <div className={styles.row}>
              <div>
                <p className={styles.value}>{diversification}%</p>
                <p
                  className={
                    styles.label + " " + styles[diversificationLevel()]
                  }
                >
                  {diversificationText[diversificationLevel()]}
                </p>
              </div>
              <Gauge
                sx={{
                  "& .MuiGauge-valueArc": {
                    fill: statusColors[diversificationLevel()]
                  },
                  "& .MuiGauge-valueText": { display: "none" }
                }}
                width={90}
                height={60}
                value={diversification}
                startAngle={-90}
                endAngle={90}
                innerRadius="65%"
                outerRadius="100%"
              />
            </div>
          </div>
        </AppCard>
      </div>
      <div className={styles.container}>
        <AppCard>
          {!loading && dashboard && (
            <AllocationHandler
              allocations={mockAllocations}
              title="Målalloekering över instrumenttyp"
              onSave={(values) => {
                console.log(values);
              }}
            />
          )}
        </AppCard>
        <AppCard>
          <DonutChart
            help={{
              title: "Vad betyder instrumenttyp?",
              content:
                "Instrumenttyp beskriver vilken sorts finansiell produkt det är, till exempel en aktie, fond eller obligation."
            }}
            title="Aktuell fördelning per instrumenttyp"
            data={mockAllocations}
          />
        </AppCard>
      </div>
      <div className={styles.container}>
        <InnehavsLista
          holdings={holdings}
          columns={[
            "ticker",
            "instrumentName",
            "quantity",
            "avgBuyPrice",
            "account_type",
            "account_name",
            "marketValue",
            "pnl",
            "pnlPct"
          ]}
        />
      </div>
    </>
  );
}

export default PortfolioHealthPage;
