import { PieChart } from "@mui/x-charts";
import { useMediaQuery, useTheme } from "@mui/material";
import styles from "./AllocationChart.module.css";
import AppCard from "./AppCard";
import {
  getPortfolio,
  type AllocationRow
} from "../services/allocationService";
import { useEffect, useState } from "react";

const AllocationChart = () => {
  const [allocationRows, setAllocationRows] = useState<AllocationRow[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getPortfolio()
      .then((portfolio) => {
        setAllocationRows(portfolio.allocationRows);
      })
      .catch(() => {
        setError("Kunde inte hämta fördelningen");
      });
  }, []);

  const total = allocationRows.reduce((sum, row) => sum + row.actual, 0);

  const data = allocationRows.map((row) => ({
    label: `${row.accountType} (${((row.actual / total) * 100).toFixed(0)}%)`,
    value: row.actual
  }));

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
                  item ? `${((item.value / total) * 100).toFixed(0)}%` : ""
              }
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
