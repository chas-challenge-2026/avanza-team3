import {
  Box,
  Alert,
  Snackbar,
  MenuItem,
  TextField,
  Autocomplete,
} from "@mui/material";
import AppButton from "./AppButton";
import styles from "./InnehavForm.module.css";
import { useState, useEffect } from "react";
import AppCard from "./AppCard";
import { useHoldings } from "../hooks/useHoldings";
import { useAccounts } from "../hooks/useAccounts";
import { getInstruments } from "../services/instrumentService";
import type { Instrument } from "../types/Instrument";

type Currency = "SEK" | "USD" | "";

type InnehavFormData = {
  accountId: string;
  ticker: string;
  instrumentName: string;
  quantity: string;
  avgBuyPrice: string;
  currency: Currency;
};

const initialFormDataValue: InnehavFormData = {
  accountId: "",
  ticker: "",
  instrumentName: "",
  quantity: "",
  avgBuyPrice: "",
  currency: "",
};

const InnehavsForm = () => {
  const { addHolding } = useHoldings();
  const { accounts } = useAccounts();

  const [instruments, setInstruments] = useState<Instrument[]>([]);
  const [formData, setFormData] =
    useState<InnehavFormData>(initialFormDataValue);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [openSnackbar, setOpenSnackbar] = useState(false);
  const [selectedInstrument, setSelectedInstrument] =
    useState<Instrument | null>(null);

  useEffect(() => {
    const loadInstruments = async () => {
      try {
        const data = await getInstruments();
        setInstruments(data);
      } catch (error) {
        console.error("Kunde inte hämta instrument", error);
      }
    };

    loadInstruments();
  }, []);

  const handleSubmit = async (event: React.SubmitEvent<HTMLFormElement>) => {
    event.preventDefault();

    setSuccessMessage("");

    if (!handleValidate()) {
      return;
    }

    setIsSubmitting(true);

    try {
      const submittedData = {
        accountId: Number(formData.accountId),
        ticker: formData.ticker.trim().toUpperCase(),
        quantity: Number(formData.quantity),
        avgBuyPrice: Number(formData.avgBuyPrice),
      };
      console.log(submittedData);
      await addHolding(submittedData);

      setFormData(initialFormDataValue);
      setSelectedInstrument(null);
      setErrors({});
      setSuccessMessage("Innehavet har lagts till");
      setOpenSnackbar(true);
    } catch (error) {
      setErrors({
        submit: error instanceof Error ? error.message : "Något gick fel",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = event.target;

    setFormData((previusData) => ({
      ...previusData,
      [name]: value,
    }));
    console.log(event.target.value);
  };

  const handleValidate = () => {
    const newErrors: Record<string, string> = {};

    if (!formData.accountId) {
      newErrors.accountId = "Välj ett konto";
    }
    const quantity = Number(formData.quantity);

    if (!formData.quantity || quantity <= 0) {
      newErrors.quantity = "Antalet måste vara större än 0";
    }

    const avgBuyPrice = Number(formData.avgBuyPrice);

    if (!formData.avgBuyPrice || avgBuyPrice < 0) {
      newErrors.avgBuyPrice = "Snittpris måste vara 0 eller högre";
    }
    if (!selectedInstrument) {
      newErrors.ticker = "Välj ett instrument";
    }

    setErrors(newErrors);

    return Object.keys(newErrors).length === 0;
  };

  const textFieldSx = {
    "& .MuiFormHelperText-root": {
      marginBottom: "3px",
      marginTop: "0px",
      fontWeight: "bold",
    },
    "& .MuiInputLabel-root.Mui-error": {
      color: "#999",
    },
    "& .MuiOutlinedInput-root.Mui-error .MuiOutlinedInput-notchedOutline": {
      borderColor: "#999",
    },
  };

  return (
    <div className={styles.formWrapper}>
      <AppCard>
        <div className={styles.titleWrapper}>
          <h2 className={styles.sectionTitle}>Lägg till innehav</h2>
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
            height: "auto",
          }}
        >
          <div className={styles.container}>
            <TextField
              fullWidth
              select
              size="small"
              label="Konto"
              name="accountId"
              value={formData.accountId}
              onChange={handleChange}
              error={Boolean(errors.accountId)}
              helperText={errors.accountId || undefined}
              sx={textFieldSx}
            >
              {accounts.map((account) => (
                <MenuItem key={account.id} value={String(account.id)}>
                  {account.accountName}
                </MenuItem>
              ))}
            </TextField>

            <Autocomplete
              options={instruments}
              value={selectedInstrument}
              getOptionLabel={(option) => `${option.ticker} - ${option.name}`}
              isOptionEqualToValue={(option, value) => option.id === value.id}
              onChange={(_, newValue) => {
                setSelectedInstrument(newValue);

                setFormData((prev) => ({
                  ...prev,
                  ticker: newValue?.ticker ?? "",
                }));
              }}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="Ticker"
                  placeholder="Sök ticker eller företag"
                  size="small"
                  error={Boolean(errors.ticker)}
                  helperText={errors.ticker || undefined}
                  sx={textFieldSx}
                />
              )}
            />

            {selectedInstrument && (
              <div className={styles.instrumentInfo}>
                <div className={styles.instrumentDetail}>
                  <span>Instrumentnamn</span>
                  <strong>{selectedInstrument.name}</strong>
                </div>

                <div className={styles.instrumentDetail}>
                  <span>Instrumenttyp</span>
                  <strong>{selectedInstrument.instrumentType}</strong>
                </div>

                <div className={styles.instrumentDetail}>
                  <span>Valuta</span>
                  <strong>{selectedInstrument.currency}</strong>
                </div>
              </div>
            )}
            <div className={styles.quantityContainer}>
              <TextField
                fullWidth
                size="small"
                label="Antal"
                name="quantity"
                type="number"
                placeholder="100"
                value={formData.quantity}
                onChange={handleChange}
                error={Boolean(errors.quantity)}
                helperText={errors.quantity || undefined}
                sx={textFieldSx}
              />

              <TextField
                fullWidth
                size="small"
                name="avgBuyPrice"
                type="number"
                placeholder="150.00"
                label="Snittpris"
                value={formData.avgBuyPrice}
                onChange={handleChange}
                error={Boolean(errors.avgBuyPrice)}
                helperText={errors.avgBuyPrice || undefined}
                sx={textFieldSx}
              />
            </div>

            <AppButton
              sx={{ maxWidth: "200px", padding: "8px" }}
              type="submit"
              variant="contained"
              disabled={isSubmitting}
            >
              {isSubmitting ? "Lägger till..." : "Lägg till"}
            </AppButton>
            <Snackbar
              open={openSnackbar}
              autoHideDuration={6000}
              onClose={() => setOpenSnackbar(false)}
              anchorOrigin={{ vertical: "top", horizontal: "center" }}
            >
              <Alert
                onClose={() => setOpenSnackbar(false)}
                severity="success"
                variant="filled"
                sx={{ width: "100%" }}
              >
                {successMessage}
              </Alert>
            </Snackbar>
          </div>
        </Box>
      </AppCard>
    </div>
  );
};
export default InnehavsForm;
