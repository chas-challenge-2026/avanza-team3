import { PieChart } from "@mui/x-charts";
import styles from "./AllocationChart.module.css";

const data = [
  { label: "Aktier", value: 400, color: "#3E73B3" },
  { label: "Fonder", value: 300, color: "#2D8F73" },
  { label: "Ränteb", value: 300, color: "#63C9C5" },
  { label: "Övrigt", value: 100, color: "#D9D9D9" }
];

const total = data.reduce((sum, item) => sum + item.value, 0);

const settings = {
  margin: { right: 5 },
  width: 200,
  height: 200,
  hideLegend: false
};

const AllocationChart = () => {
  return (
    <div className={styles.chartWrapper}>
      <div className={styles.headerWrapper}>
        <h2>Fördelning</h2>
      </div>
      <PieChart
        series={[
          {
            innerRadius: 50,
            outerRadius: 100,
            data,
            valueFormatter: (item) =>
              item ? `${((item.value / total) * 100).toFixed(0)}%` : ""
          }
        ]}
        {...settings}
      />
    </div>
  );
};
export default AllocationChart;
