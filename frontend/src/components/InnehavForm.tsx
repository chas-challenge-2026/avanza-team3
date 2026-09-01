import { Box, MenuItem, TextField, Typography } from "@mui/material";
import AppButton from "./AppButton";
import styles from "./InnehavForm.module.css";
import { useState } from "react";
import { height } from "@fortawesome/free-solid-svg-icons/fa0";

type Currency = "SEK" | "USD" | "EUR" | "";

type InnehavFormData = {
  account: string;
  ticker: string;
  instrumentName: string;
  instrumentType: string;
  quantity: string;
  avgBuyPrice: string;
  currency: Currency;
};

const initialFormDataValue: InnehavFormData = {
  account: "",
  ticker: "",
  instrumentName: "",
  instrumentType: "",
  quantity: "",
  avgBuyPrice: "",
  currency: ""
};

type InnehavsFormProps = {
  width?: string;
};

const InnehavsForm = ({ width }: InnehavsFormProps) => {
  const [formData, setFormData] =
    useState<InnehavFormData>(initialFormDataValue);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");

  const handleSubmit = (event: React.SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();

    setSuccessMessage("");

    if (!handleValidate()) {
      return;
    }

    setIsSubmitting(true);

    try {
      const submittedData = {
        ...formData,
        ticker: formData.ticker.trim().toLocaleUpperCase(),
        quantity: Number(formData.quantity),
        avgBuyPrice: Number(formData.avgBuyPrice)
      };
      console.log(submittedData);

      setFormData(initialFormDataValue);
      setErrors({});
      setSuccessMessage("Innehavet har lagts till");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = event.target;

    setFormData((previusData) => ({
      ...previusData,
      [name]: value
    }));
    console.log(event.target.value);
  };

  const handleValidate = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.account) {
      newErrors.account = "Välj ett konto";
    }
    if (!formData.ticker.trim()) {
      newErrors.ticker = "Ange en ticker";
    }
    if (!formData.instrumentName.trim()) {
      newErrors.instrumentName = "Ange ett instrumentnamn";
    }
    if (!formData.instrumentType) {
      newErrors.instrumentType = "Ange en instrumenttyp";
    }

    const quantity = Number(formData.quantity);

    if (!formData.quantity || quantity <= 0) {
      newErrors.quantity = "Antalet måste vara större än 0";
    }

    const avgBuyPrice = Number(formData.avgBuyPrice);

    if (!formData.avgBuyPrice || avgBuyPrice < 0) {
      newErrors.avgBuyPrice = "Snittpris måste vara 0 eller högre";
    }

    if (!formData.currency) {
      newErrors.currency = "Välj valuta";
    }

    setErrors(newErrors);

    return Object.keys(newErrors).length === 0;
  };

  const textFieldSx = {
    "& .MuiFormHelperText-root": {
      marginBottom: "3px",
      fontWeight: "bold"
    },
    "& .MuiInputLabel-root.Mui-error": {
      color: "#999"
    },
    "& .MuiOutlinedInput-root.Mui-error .MuiOutlinedInput-notchedOutline": {
      borderColor: "#999"
    }
  };

  return (
    <div className={styles.formWrapper}>
      <div className={styles.titleWrapper}>
        <h3>Lägg till innehav</h3>
      </div>
      <Box
        component="form"
        onSubmit={handleSubmit}
        sx={{
          display: "flex",
          flexDirection: "column",
          width: "100%"
        }}
      >
        <TextField
          select
          size="small"
          label="Konto"
          name="account"
          value={formData.account}
          onChange={handleChange}
          error={Boolean(errors.account)}
          helperText={errors.account || " "}
          sx={textFieldSx}
        >
          <MenuItem value="1">Anna ISK (ISK)</MenuItem>
          <MenuItem value="2">Anna KF (KF)</MenuItem>
          <MenuItem value="3">Anna Depå (Depa)</MenuItem>
        </TextField>

        <TextField
          size="small"
          placeholder="t.ex. ERIC-B"
          label="Ticker"
          name="ticker"
          value={formData.ticker}
          onChange={handleChange}
          error={Boolean(errors.ticker)}
          helperText={errors.ticker || " "}
          sx={textFieldSx}
        />
        <TextField
          size="small"
          name="instrumentName"
          label="Instrumentnamn"
          placeholder="t.ex. Ericsson B"
          value={formData.instrumentName}
          onChange={handleChange}
          error={Boolean(errors.instrumentName)}
          helperText={errors.instrumentName || " "}
          sx={textFieldSx}
        />

        <TextField
          select
          size="small"
          label="Instrumenttyp"
          name="instrumentType"
          value={formData.instrumentType}
          onChange={handleChange}
          error={Boolean(errors.instrumentType)}
          helperText={errors.instrumentType || " "}
          sx={textFieldSx}
        >
          <MenuItem value="Aktie">Aktie</MenuItem>
          <MenuItem value="Fond">Fond</MenuItem>
          <MenuItem value="ETF">ETF</MenuItem>
        </TextField>

        <TextField
          size="small"
          label="Antal"
          name="quantity"
          type="number"
          placeholder="100"
          value={formData.quantity}
          onChange={handleChange}
          error={Boolean(errors.quantity)}
          helperText={errors.quantity || " "}
          sx={textFieldSx}
        />

        <TextField
          size="small"
          name="avgBuyPrice"
          type="number"
          placeholder="150.00"
          label="Snittpris"
          value={formData.avgBuyPrice}
          onChange={handleChange}
          error={Boolean(errors.avgBuyPrice)}
          helperText={errors.avgBuyPrice || " "}
          sx={textFieldSx}
        />

        <TextField
          select
          size="small"
          label="Valuta"
          name="currency"
          value={formData.currency}
          onChange={handleChange}
          error={Boolean(errors.currency)}
          helperText={errors.currency || " "}
          sx={textFieldSx}
        >
          <MenuItem value="SEK">SEK</MenuItem>
          <MenuItem value="USD">USD</MenuItem>
          <MenuItem value="EUR">EUR</MenuItem>
        </TextField>
        <AppButton type="submit" variant="contained" disabled={isSubmitting}>
          {isSubmitting ? "Lägger till..." : "Lägg till"}
        </AppButton>
      </Box>
      {successMessage && (
        <Typography
          sx={{ fontWeight: 800, m: "auto" }}
          className={styles.successMessage}
        >
          {successMessage}
        </Typography>
      )}
    </div>
  );
};
export default InnehavsForm;
