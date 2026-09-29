import { Box, LinearProgress, Typography } from "@mui/material";
import AppCard from "./AppCard";

const industries = [
  { name: "Teknik", value: 68 },
  { name: "Finans", value: 42 },
  { name: "Hälsovård", value: 28 },
  { name: "Industri", value: 15 },
  { name: "Fastigheter", value: 10 },
  { name: "Övrigt", value: 7 }
];

const colors = [
  "#1976d2",
  "#00a86b",
  "#f5a623",
  "#e74c3c",
  "#9aa8b8",
  "#60758d"
];

const IndustriesChart = () => {
  return (
    <AppCard>
      <h2>Branschfördelning</h2>
      {industries.map((industry, index) => (
        <Box
          key={industry.name}
          sx={{
            display: "flex",
            alignItems: "center",
            gap: 1,
            mb: 2
          }}
        >
          <Typography
            sx={{
              width: 75,
              fontFamily: "Roboto",
              fontSize: "13px",
              fontWeight: "500",
              color: "#5a6569"
            }}
          >
            {industry.name}
          </Typography>

          <LinearProgress
            variant="determinate"
            value={industry.value}
            sx={{
              flex: 1,
              height: 7,
              borderRadius: 5,
              backgroundColor: "#e5eaf0",

              "& .MuiLinearProgress-bar": {
                borderRadius: 5,
                backgroundColor: colors[index]
              }
            }}
          />

          <Typography
            sx={{
              fontFamily: "Roboto",
              fontSize: "13px",
              fontWeight: "500",
              color: "#5a6569"
            }}
          >
            {industry.value}%
          </Typography>
        </Box>
      ))}
    </AppCard>
  );
};
export default IndustriesChart;
