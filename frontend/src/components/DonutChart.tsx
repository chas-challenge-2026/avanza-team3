import { PieChart } from "@mui/x-charts";
import { useMediaQuery, useTheme } from "@mui/material";
import styles from "./AllocationChart.module.css";
import AppCard from "./AppCard";

type DonutChartItem = {
  label: string;
  value: number;
  color?: string;
};

type DonutChartProps = {
  title?: string;
  data: DonutChartItem[];
};

const DonutChart = ({ title, data }: DonutChartProps) => {
  const total = data.reduce((sum, item) => sum + item.value, 0);

  const pieData = data.map((item) => ({
    label: `${item.label} (${((item.value / total) * 100).toFixed(0)}%)`,
    value: item.value,
    color: item.color
  }));

  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

  const innerRadius = isMobile ? 35 : 50;
  const outerRadius = isMobile ? 70 : 100;

  return (
    <AppCard>
      <div className={styles.chartWrapper}>
        <div className={styles.headerWrapper}>
          <h2>{title}</h2>
        </div>
        <div className={styles.chartBox}>
          <PieChart
            series={[
              {
                innerRadius,
                outerRadius,
                data: pieData,
                cornerRadius: 5,
                paddingAngle: 0.5,
                valueFormatter: (item) =>
                  item ? `${((item.value / total) * 100).toFixed(0)}%` : ""
              }
            ]}
            slotProps={{
              legend: {
                sx: {
                  "& .MuiChartsLegend-label": {
                    fontSize: "13px",
                    fontWeight: 500,
                    fontFamily: "Roboto",
                    color: "#5a6569"
                  }
                }
              }
            }}
            margin={{ right: 5 }}
            hideLegend={false}
          />
        </div>
      </div>
    </AppCard>
  );
};
export default DonutChart;
