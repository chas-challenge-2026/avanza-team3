import { Box, MenuItem, TextField, Typography } from "@mui/material";
import AppButton from "./AppButton";
import styles from "./InnehavForm.module.css";

type Currency = "SEK" | "USD" | "EUR";

type InnehavFormData = {
  account: string;
  ticker: string;
  instrumentName: string;
  quantity: number;
  avgBuyPrice: number;
  currency: Currency;
};

// sätt onChange på textfields och lagra i state

const InnehavsForm = () => {
  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    console.log(event.target);
  };
  return (
    <div className={styles.formWrapper}>
      <div className={styles.titleWrapper}>
        <Typography variant="h5">Lägg till innehav</Typography>
      </div>
      <Box
        component="form"
        onSubmit={handleSubmit}
        sx={{
          display: "flex",
          flexDirection: "column",
          gap: 2,
          width: "100%"
        }}
      >
        <TextField select label="Konto" name="account">
          <MenuItem value="1">Anna ISK (ISK)</MenuItem>
          <MenuItem value="2">Anna KF (KF)</MenuItem>
          <MenuItem value="3">Anna Depå (Depa)</MenuItem>
        </TextField>

        <TextField
          id="outlined-helperText"
          label="Ticker"
          name="ticker"
          placeholder="t.ex. Ericsson B"
        />
        <TextField
          id="outlined-helperText"
          name="instrumentName"
          label="Instrumentnamn"
          placeholder="t.ex. ERIC-B"
        />
        <TextField label="Antal" name="quantity" placeholder="100" />

        <TextField
          id="outlined-helperText"
          name="avgBuyPrice"
          placeholder="150.00"
          label="Snittpris"
        />

        <TextField select label="Valuta" name="currency" defaultValue="SEK">
          <MenuItem value="SEK">SEK</MenuItem>
          <MenuItem value="USD">USD</MenuItem>
          <MenuItem value="EUR">EUR</MenuItem>
        </TextField>
        <AppButton type="submit" variant="contained">
          Lägg till
        </AppButton>
      </Box>
    </div>
  );
};
export default InnehavsForm;
