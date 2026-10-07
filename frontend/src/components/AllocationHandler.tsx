import { Box, Grid, Input, Slider } from "@mui/material";
import styles from "./AllocationHandler.module.css";
import { useState, type ChangeEvent } from "react";
import AppButton from "./AppButton";

type AllocationItem = {
  accountType: string;
  value: number;
};

type AllocationHandlerProps = {
  allocations: AllocationItem[];
  onSave?: (values: Record<string, number>) => void;
};

const AllocationHandler = ({ allocations, onSave }: AllocationHandlerProps) => {
  const [values, setValues] = useState<Record<string, number>>(
    Object.fromEntries(
      allocations.map(({ accountType, value }) => [accountType, value])
    )
  );

  const handleValueChange = (accountType: string, value: number) => {
    setValues((current) => ({
      ...current,
      [accountType]: value
    }));
  };

  const handleSaveGoal = () => {
    onSave?.(values);
  };

  const handleReset = () => {
    setValues(
      Object.fromEntries(allocations.map(({ accountType }) => [accountType, 0]))
    );
  };

  const totalAllocation = Object.values(values).reduce(
    (sum, value) => sum + value,
    0
  );
  return (
    <>
      <h2 className={styles.sectionTitle}>Målallokering</h2>
      <p className={styles.label}>
        Justera fördelning med reglagen för önskad fördelning. <br />
        Summan <u>måste</u> vara 100%.
      </p>
      {allocations.map((allocation) => (
        <div className={styles.wrapper} key={allocation.accountType}>
          <div className={styles.content}>
            <p className={styles.label}>{allocation.accountType}</p>
            <Grid container spacing={2} sx={{ alignItems: "center" }}>
              <Grid size="grow">
                <Slider
                  sx={{ height: 10 }}
                  value={values[allocation.accountType] ?? 0}
                  onChange={(_, newValue) => {
                    if (typeof newValue === "number") {
                      handleValueChange(allocation.accountType, newValue);
                    }
                  }}
                  min={0}
                  max={100}
                />
              </Grid>
              <Input
                value={values[allocation.accountType] ?? 0}
                size="small"
                onChange={(event: ChangeEvent<HTMLInputElement>) => {
                  const value =
                    event.target.value === "" ? 0 : Number(event.target.value);

                  handleValueChange(allocation.accountType, value);
                }}
                inputProps={{
                  min: 0,
                  max: 100,
                  type: "number",
                  "aria-label": `${allocation.accountType} procent`
                }}
              />
              <p>%</p>
            </Grid>
          </div>
        </div>
      ))}
      <p
        className={styles.label}
        style={{ color: totalAllocation > 100 ? "red" : "#5a6569" }}
      >
        Din totala fördelning: {totalAllocation}%
      </p>
      <Box sx={{ display: "flex", justifyContent: "space-between", mt: 2 }}>
        <AppButton onClick={handleReset} variant="contained">
          Återställ
        </AppButton>
        <AppButton
          disabled={totalAllocation !== 100}
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
