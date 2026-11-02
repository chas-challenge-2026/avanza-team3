import { useState } from "react";
import { Alert, Box, Slider, Snackbar, Typography } from "@mui/material";
import styles from "./AllocationHandler.module.css";
import AppButton from "./AppButton";
import type { AccountAllocationTarget } from "../types/accountAllocationTargets";

type SliderValue = [number, number];

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

const getInitialSliderValue = (
  targets: AccountAllocationTarget[]
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
    Math.round(((stocks + bonds) / total) * 100)
  ];
};

const NewAllocationhandler = ({
  allocations,
  onSave
}: NewAllocationHandlerProps) => {
  const [openSnackbar, setOpenSnackbar] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [values, setValues] = useState<Record<string, SliderValue>>(
    Object.fromEntries(
      allocations.map(({ id, targets }) => [id, getInitialSliderValue(targets)])
    )
  );
  const handleValueChange = (id: string, value: SliderValue) => {
    setValues((current) => ({
      ...current,
      [id]: value
    }));
  };

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
          : "Kunde inte spara målallokeringen"
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
              width: "100%"
            }}
          >
            <Box
              sx={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center"
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
              #5875f5 0%,
              #5875f5 ${first}%,
              #2bb887 ${first}%,
              #2bb887 ${second}%,
              #f5a623 ${second}%,
              #f5a623 100%
            )`,
                  borderRadius: "5px"
                },

                "& .MuiSlider-thumb": {
                  borderRadius: 2,
                  width: 35,
                  height: 35,
                  bgcolor: "#fff",
                  border: "2px solid #8491a5",

                  "&:before": {
                    boxShadow: "none"
                  },

                  "&:after": {
                    width: 8,
                    height: 8,
                    borderRadius: "50%",
                    bgcolor: "#64748b",
                    boxShadow: "none"
                  },

                  "&:hover, &.Mui-focusVisible": {
                    boxShadow: "0 0 0 3px rgba(255,255,255,0.15)"
                  }
                },

                "& .MuiSlider-track": {
                  display: "none"
                }
              }}
            />

            <Box
              sx={{
                display: "flex",
                justifyContent: "space-between",
                mt: 2,
                gap: 1
              }}
            >
              <Typography
                variant="body2"
                sx={{ color: "#5875f5", fontWeight: 500 }}
              >
                Aktier: {stocks}%
              </Typography>

              <Typography
                variant="body2"
                sx={{ color: "#2bb887", fontWeight: 500 }}
              >
                Ränta: {rates}%
              </Typography>

              <Typography
                variant="body2"
                sx={{ color: "#f5a623", fontWeight: 500 }}
              >
                Övrigt: {other}%
              </Typography>
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
