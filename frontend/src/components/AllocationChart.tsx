import { PieChart } from "@mui/x-charts";
import { useMediaQuery, useTheme } from "@mui/material";
import styles from "./AllocationChart.module.css";
import AppCard from "./AppCard";

const data = [
  { label: "Aktier", value: 400, color: "#3E73B3" },
  { label: "Fonder", value: 300, color: "#2D8F73" },
  { label: "Ränteb", value: 300, color: "#63C9C5" },
  { label: "Övrigt", value: 100, color: "#D9D9D9" },
];

const total = data.reduce((sum, item) => sum + item.value, 0);

const AllocationChart = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

  const innerRadius = isMobile ? 35 : 50;
  const outerRadius = isMobile ? 70 : 100;

  return (
    <AppCard>
      <div className={styles.chartWrapper}>
        <div className={styles.headerWrapper}>
          <h2>Fördelning (marknadsvärde)</h2>
        </div>
        <div className={styles.chartBox}>
          <PieChart
            series={[
              {
                innerRadius,
                outerRadius,
                data,
                valueFormatter: (item) =>
                  item ? `${((item.value / total) * 100).toFixed(0)}%` : "",
              },
            ]}
            margin={{ right: 5 }}
            hideLegend={false}
          />
        </div>
      </div>
    </AppCard>
  );
};
export default AllocationChart;
