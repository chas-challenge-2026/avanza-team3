import { createTheme } from "@mui/material/styles";

const theme = createTheme({
  palette: {
    primary: { main: "#3d7f6e", contrastText: "#fff" },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 999,
          textTransform: "none",
          fontWeight: 600,
          fontSize: "0.9rem",
          padding: "10px 14px",
          boxShadow: "none",
        },
        contained: {
          backgroundColor: "#e6f4ef",
          color: "#057a5b",
          "&:hover": {
            backgroundColor: "#d3ece3",
            boxShadow: "none",
          },
          "&.Mui-disabled": {
            backgroundColor: "#f0f1f2",
            color: "#9aa3a6",
          },
        },
      },
    },
  },
});

export default theme;
