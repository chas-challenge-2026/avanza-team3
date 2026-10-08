import { PieChart } from "@mui/x-charts";
import { useMediaQuery, useTheme } from "@mui/material";
import styles from "./DonutChart.module.css";
import PopOver from "./Popover";

type DonutChartItem = {
  label: string;
  value: number;
  color?: string;
  target?: number;
};

type DonutChartProps = {
  title: string;
  data: DonutChartItem[];
  help: {
    title: string;
    content: string;
  };
};

const DonutChart = ({ title, data, help }: DonutChartProps) => {
  const total = data.reduce((sum, item) => sum + item.value, 0);

  const pieData = data.map((item) => {
    const percentage = ((item.value / total) * 100).toFixed(0);
    const deviationText =
      item.target === undefined
        ? ""
        : item.value === item.target
          ? "\nIngen avvikelse"
          : `\nAvviker ${
              item.value - item.target > 0 ? "+" : ""
            }${(item.value - item.target).toFixed(0)}% från mål`;

    return {
      label: `${item.label} ${percentage}%${deviationText}`,
      value: item.value,
      color: item.color
    };
  });

  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

  const innerRadius = isMobile ? 35 : 50;
  const outerRadius = isMobile ? 70 : 100;

  return (
    <>
      <div className={styles.chartWrapper}>
        <div className={styles.headerWrapper}>
          <h2 className={styles.h2}>{title}</h2>
          <PopOver title={help.title} content={help.content} />
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
                    color: "#5a6569",
                    whiteSpace: "pre-line",
                    lineHeight: 1.7
                  }
                }
              }
            }}
            margin={{ right: 5 }}
            hideLegend={false}
          />
        </div>
      </div>
    </>
  );
};
export default DonutChart;
