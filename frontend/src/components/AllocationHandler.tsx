import { Box, Grid, Input, Slider, type SxProps } from "@mui/material";
import styles from "./AllocationHandler.module.css";
import { useState, type ChangeEvent } from "react";
import type { Theme } from "@emotion/react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faChartLine,
  faMoneyBill1,
  faPiggyBank
} from "@fortawesome/free-solid-svg-icons";
import AppButton from "./AppButton";

type AllocationHandlerProps = {
  sx?: SxProps<Theme>;
};

const AllocationHandler = ({ sx }: AllocationHandlerProps) => {
  const [stocks, setStocks] = useState(60);
  const [funds, setFunds] = useState(30);
  const [cash, setCash] = useState(10);
  const [error, setError] = useState("");

  const handleStocksChange = (event: Event, newValue: number) => {
    setStocks(newValue);
  };
  const handleFundsChange = (event: Event, newValue: number) => {
    setFunds(newValue);
  };
  const handleCashChange = (event: Event, newValue: number) => {
    setCash(newValue);
  };

  const handleInputChange = (event: ChangeEvent<HTMLInputElement>) => {
    setStocks(event.target.value === "" ? 0 : Number);
    setFunds(event.target.value === "" ? 0 : Number);
    setCash(event.target.value === "" ? 0 : Number);
  };
  const totalAllicationValue = stocks + funds + cash;

  const handleReset = () => {
    setStocks(0);
    setFunds(0);
    setCash(0);
  };

  const handleSaveGoal = () => {
    if (totalAllicationValue != 100) {
      setError("Din totala fördelning måste vara 100%");
    }
  };

  return (
    <>
      <h2>Målallokering</h2>
      <p className={styles.label}>
        Justera fördelning med reglagen. Summan måste vara 100%.
      </p>
      <div className={styles.wrapper}>
        <FontAwesomeIcon
          icon={faChartLine}
          className={`${styles.icon} ${styles.iconCircle}`}
          style={{ backgroundColor: "green" }}
        />
        <div className={styles.content}>
          <p className={styles.label} id="input-slider">
            Aktier
          </p>
          <Grid container spacing={2} sx={{ alignItems: "center" }}>
            <Grid size="grow">
              <Slider
                aria-label=""
                value={stocks}
                onChange={handleStocksChange}
                sx={{ color: "green", height: 8 }}
              />
            </Grid>
            <Input
              value={stocks}
              size="small"
              onChange={handleInputChange}
              inputProps={{
                min: 0,
                max: 100,
                type: "number",
                "aria-labelledby": "input-slider"
              }}
            />
            <p>%</p>
          </Grid>
        </div>
      </div>
      <div className={styles.wrapper}>
        <FontAwesomeIcon
          icon={faPiggyBank}
          className={`${styles.icon} ${styles.iconCircle}`}
          style={{ backgroundColor: "#1976d2" }}
        />
        <div className={styles.content}>
          <p className={styles.label} id="input-slider">
            Fonder
          </p>
          <Grid container spacing={2} sx={{ alignItems: "center" }}>
            <Grid size="grow">
              <Slider
                aria-label=""
                value={funds}
                onChange={handleFundsChange}
                sx={{ height: 8 }}
              />
            </Grid>
            <Input
              value={funds}
              size="small"
              onChange={handleInputChange}
              inputProps={{
                min: 0,
                max: 100,
                type: "number",
                "aria-labelledby": "input-slider"
              }}
            />
            <p>%</p>
          </Grid>
        </div>
      </div>
      <div className={styles.wrapper}>
        <FontAwesomeIcon
          icon={faMoneyBill1}
          className={`${styles.icon} ${styles.iconCircle}`}
          style={{ backgroundColor: "#8b69a7" }}
        />
        <div className={styles.content}>
          <p className={styles.label} id="input-slider">
            Kontanter
          </p>
          <Grid container spacing={2} sx={{ alignItems: "center" }}>
            <Grid size="grow">
              <Slider
                aria-label=""
                value={cash}
                onChange={handleCashChange}
                sx={{ color: "#8b69a7", height: 8 }}
              />
            </Grid>
            <Input
              value={cash}
              size="small"
              onChange={handleInputChange}
              inputProps={{
                min: 0,
                max: 100,
                type: "number",
                "aria-labelledby": "input-slider"
              }}
            />
            <p>%</p>
          </Grid>
        </div>
      </div>
      <p
        className={styles.label}
        style={{ color: totalAllicationValue > 100 ? "red" : "#5a6569" }}
      >
        Din totala fördelning: {totalAllicationValue}%
      </p>
      <br />
      <Box
        sx={{
          display: "flex",
          width: "100%",
          height: 32,
          borderRadius: 2,
          overflow: "hidden"
        }}
      >
        <Box
          sx={{
            width: `${stocks}%`,
            backgroundColor: "success.main"
          }}
        />
        <Box
          sx={{
            width: `${funds}%`,
            backgroundColor: "primary.main"
          }}
        />
        <Box
          sx={{
            width: `${cash}%`,
            backgroundColor: "#8b69a7"
          }}
        />
      </Box>
      {/* <Box
        sx={{
          display: "flex",
          justifyContent: "space-between",
          mt: 2
        }}
      >
        <Box>
          <p className={styles.label}>● Aktier</p>
          <p className={styles.label}>{stocks}%</p>
        </Box>

        <Box>
          <p className={styles.label}>● Fonder</p>
          <p className={styles.label}>{funds}%</p>
        </Box>

        <Box>
          <p className={styles.label}>● Kontanter</p>
          <p className={styles.label}>{cash}%</p>
        </Box>
      </Box> */}
      <Box sx={{ display: "flex", justifyContent: "space-between", mt: 2 }}>
        <AppButton onClick={handleReset} variant="contained">
          Återställ
        </AppButton>
        <AppButton
          disabled={totalAllicationValue !== 100}
          onClick={handleSaveGoal}
          variant="contained"
        >
          Spara
        </AppButton>
      </Box>
    </>
  );
};
export default AllocationHandler;
