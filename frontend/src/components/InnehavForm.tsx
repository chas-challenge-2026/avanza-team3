import { Box, Container, MenuItem, TextField, Typography } from "@mui/material";
import AppButton from "./AppButton";
import styles from "./InnehavForm.module.css";
import { useState } from "react";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faBriefcase } from "@fortawesome/free-solid-svg-icons";
import AppCard from "./AppCard";

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

const InnehavsForm = () => {
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

  const onlyLettersRegex = /^[A-Za-zÅÄÖåäö\s]+$/;
  const tickerRegex = /^[A-Za-zÅÄÖåäö0-9.-]+$/;

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
    } else if (!tickerRegex.test(formData.ticker.trim())) {
      newErrors.ticker = "Ticker får bara innehålla bokstäver, siffror och -";
    }
    if (!formData.instrumentName.trim()) {
      newErrors.instrumentName = "Ange ett instrumentnamn";
    } else if (!onlyLettersRegex.test(formData.instrumentName.trim())) {
      newErrors.instrumentName = "Instrumentnamn får bara innehålla bokstäver";
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
    <AppCard>
      <div className={styles.formWrapper}>
        <div className={styles.titleWrapper}>
          <FontAwesomeIcon icon={faBriefcase} className={styles.icon} />
          <Typography sx={{ fontSize: "28px", fontWeight: 700 }}>
            Lägg till innehav
          </Typography>
        </div>
        <p className={styles.label}>
          Fyll i uppgifterna nedanför för att lägga till ett nytt innehav i din
          portfölj.
        </p>
        <Box
          component="form"
          onSubmit={handleSubmit}
          sx={{
            display: "flex",
            flexDirection: "row",
            justifyContent: "space-between",
            width: "100%",
            height: "auto"
          }}
        >
          <div className={styles.containerRight}>
            <TextField
              select
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
          </div>

          <div className={styles.containerLeft}>
            <TextField
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
            <AppButton
              sx={{ maxWidth: "200px", padding: "15px" }}
              type="submit"
              variant="contained"
              disabled={isSubmitting}
            >
              {isSubmitting ? "Lägger till..." : "Lägg till"}
            </AppButton>
          </div>
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
    </AppCard>
  );
};
export default InnehavsForm;
