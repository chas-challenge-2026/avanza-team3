import { useState } from "react";
import { Alert, Box, Slider, Snackbar, Typography } from "@mui/material";
import styles from "./AllocationHandler.module.css";
import AppButton from "./AppButton";
import type { AccountAllocationTarget } from "../types/accountAllocationTargets";

type SliderValue = [number, number];
type AllocationField = "stocks" | "rates" | "other";

const INITIAL_SLIDER_VALUE: SliderValue = [33, 66];

type AllocationItem = {
  id: string;
  label: string;
  targets: AccountAllocationTarget[];
};

type NewAllocationHandlerProps = {
  allocations: AllocationItem[];
  onSave?: (values: Record<string, SliderValue>) => Promise<void>;
};

const COLORS = {
  STOCK: "#3B82F6",
  BOND: "#F59E0B",
  OTHER: "#94A3B8",
};

const getInitialSliderValue = (
  targets: AccountAllocationTarget[],
): SliderValue => {
  const stocks =
    targets.find((target) => target.category === "STOCK")?.targetPercentage ??
    0;
  const bonds =
    targets.find((target) => target.category === "BOND")?.targetPercentage ?? 0;
  const other =
    targets.find((target) => target.category === "OTHER")?.targetPercentage ??
    0;

  const total = stocks + bonds + other;

  if (total === 0) {
    return INITIAL_SLIDER_VALUE;
  }

  return [
    Math.round((stocks / total) * 100),
    Math.round(((stocks + bonds) / total) * 100),
  ];
};

const NewAllocationhandler = ({
  allocations,
  onSave,
}: NewAllocationHandlerProps) => {
  const [openSnackbar, setOpenSnackbar] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [values, setValues] = useState<Record<string, SliderValue>>(
    Object.fromEntries(
      allocations.map(({ id, targets }) => [
        id,
        getInitialSliderValue(targets),
      ]),
    ),
  );

  const handleValueChange = (id: string, value: SliderValue) => {
    setValues((current) => ({
      ...current,
      [id]: value,
    }));
  };

  const handleAllocationInput = (
    id: string,
    field: AllocationField,
    rawValue: string,
  ) => {
    const value = Math.max(0, Math.min(100, Math.round(Number(rawValue) || 0)));

    setValues((current) => {
      const [first, second] = current[id] ?? INITIAL_SLIDER_VALUE;
      const stocks = first;
      const rates = second - first;

      if (field === "stocks") {
        const nextStocks = Math.min(value, 100 - rates);
        return { ...current, [id]: [nextStocks, nextStocks + rates] };
      }

      if (field === "rates") {
        const nextRates = Math.min(value, 100 - stocks);
        return { ...current, [id]: [stocks, stocks + nextRates] };
      }

      const nextOther = Math.min(value, 100 - stocks);
      return { ...current, [id]: [first, 100 - nextOther] };
    });
  };

  const renderAllocationValue = (
    id: string,
    field: AllocationField,
    label: string,
    value: number,
    color: string,
  ) => (
    <Typography
      component="label"
      variant="body2"
      sx={{ color, fontWeight: 500 }}
    >
      {label}:{" "}
      <input
        type="number"
        min={0}
        max={100}
        step={1}
        value={value}
        aria-label={`${label} i procent`}
        onFocus={(event) => event.currentTarget.select()}
        onChange={(event) =>
          handleAllocationInput(id, field, event.target.value)
        }
        style={{
          width: "3em",
          border: 0,
          padding: 0,
          background: "transparent",
          color: "inherit",
          font: "inherit",
        }}
      />
      %
    </Typography>
  );

  const handleSaveGoal = async () => {
    setSuccessMessage("");

    try {
      await onSave?.(values);
      setSuccessMessage("Din önskade målallokering är uppdaterad!");
      setOpenSnackbar(true);
    } catch (error) {
      setSuccessMessage(
        error instanceof Error
          ? error.message
          : "Kunde inte spara målallokeringen",
      );
      setOpenSnackbar(true);
    }
  };

  return (
    <>
      {allocations.map((allocation) => {
        const [first, second] = values[allocation.id] ?? INITIAL_SLIDER_VALUE;
        const stocks = first;
        const rates = second - first;
        const other = 100 - second;

        return (
          <Box
            key={allocation.id}
            sx={{
              borderRadius: 3,
              pb: 3,
              width: "100%",
            }}
          >
            <Box
              sx={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
              }}
            >
              <h2 className={styles.label}>{allocation.label}</h2>
            </Box>

            <Slider
              value={values[allocation.id] ?? INITIAL_SLIDER_VALUE}
              onChange={(_, newValue) => {
                if (Array.isArray(newValue)) {
                  handleValueChange(allocation.id, [newValue[0], newValue[1]]);
                }
              }}
              min={0}
              max={100}
              step={1}
              disableSwap
              track={false}
              aria-label={`Målallokering för ${allocation.label}`}
              sx={{
                height: 19,
                padding: "10px 0",

                "& .MuiSlider-rail": {
                  opacity: 1,
                  background: `linear-gradient(
                    to right,
                    ${COLORS.STOCK} 0%,
                    ${COLORS.STOCK} ${first}%,
                    ${COLORS.BOND} ${first}%,
                    ${COLORS.BOND} ${second}%,
                    ${COLORS.OTHER} ${second}%,
                    ${COLORS.OTHER} 100%
                  )`,
                  borderRadius: "5px",
                },

                "& .MuiSlider-thumb": {
                  borderRadius: 2,
                  width: 35,
                  height: 35,
                  bgcolor: "#fff",
                  border: "2px solid #8491a5",

                  "&:before": {
                    boxShadow: "none",
                  },

                  "&:after": {
                    width: 8,
                    height: 8,
                    borderRadius: "50%",
                    bgcolor: "#64748b",
                    boxShadow: "none",
                  },

                  "&:hover, &.Mui-focusVisible": {
                    boxShadow: "0 0 0 3px rgba(255,255,255,0.15)",
                  },
                },

                "& .MuiSlider-track": {
                  display: "none",
                },
              }}
            />

            <Box
              sx={{
                display: "flex",
                justifyContent: "space-between",
                mt: 2,
                gap: 1,
              }}
            >
              {renderAllocationValue(
                allocation.id,
                "stocks",
                "Aktier",
                stocks,
                COLORS.STOCK,
              )}

              {renderAllocationValue(
                allocation.id,
                "rates",
                "Ränta",
                rates,
                COLORS.BOND,
              )}

              {renderAllocationValue(
                allocation.id,
                "other",
                "Övrigt",
                other,
                COLORS.OTHER,
              )}
            </Box>
          </Box>
        );
      })}
      <Box sx={{ display: "flex", justifyContent: "flex-end", mt: 2 }}>
        <AppButton onClick={handleSaveGoal} variant="contained">
          Spara
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
      </Box>
    </>
  );
};
export default NewAllocationhandler;
