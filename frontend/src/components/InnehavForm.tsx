import { Box, Alert, Snackbar, TextField, Autocomplete } from "@mui/material";
import AppButton from "./AppButton";
import styles from "./InnehavForm.module.css";
import { useState, useEffect } from "react";
import AppCard from "./AppCard";
import { getInstruments } from "../services/instrumentService";
import type { Instrument } from "../types/Instrument";

type Props = {
  onAdd: (instrument: Instrument) => void;
};

const InnehavsForm = ({ onAdd }: Props) => {
  const [instruments, setInstruments] = useState<Instrument[]>([]);
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
      if (selectedInstrument) {
        onAdd(selectedInstrument);
      }

      setSelectedInstrument(null);
      setErrors({});
      setSuccessMessage("Instrumentet har lagts till");
      setOpenSnackbar(true);
    } catch (error) {
      setErrors({
        submit: error instanceof Error ? error.message : "Något gick fel",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleValidate = () => {
    const newErrors: Record<string, string> = {};

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
          <h2 className={styles.sectionTitle}>Lägg till gödkänt innehav</h2>
        </div>
        <p className={styles.label}>
          Välj ett instrument som får användas vid rebalansering.
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
            <Autocomplete
              options={instruments}
              value={selectedInstrument}
              getOptionLabel={(option) => `${option.ticker} - ${option.name}`}
              isOptionEqualToValue={(option, value) => option.id === value.id}
              onChange={(_, newValue) => {
                setSelectedInstrument(newValue);
              }}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="Instrument"
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
