import { Button } from "@mui/material";
import type { Theme } from "@mui/material/styles";
import type { SxProps } from "@mui/material/styles";

type AppButtonProps = {
  children: React.ReactNode;
  variant?: "outlined" | "contained";
  sx?: SxProps<Theme>;
  type?: "button" | "submit";
  disabled?: boolean;
};

const AppButton = ({
  children,
  variant,
  sx,
  type,
  disabled
}: AppButtonProps) => {
  return (
    <Button sx={sx} variant={variant} type={type} disabled={disabled}>
      {children}
    </Button>
  );
};
export default AppButton;
