import { Button } from "@mui/material";
import type { Theme } from "@mui/material/styles";
import type { SxProps } from "@mui/material/styles";

type AppButtonProps = {
  children: React.ReactNode;
  variant?: "outlined" | "contained";
  sx?: SxProps<Theme>;
  type?: "button" | "submit";
};

const AppButton = ({ children, variant, sx, type }: AppButtonProps) => {
  return (
    <Button sx={sx} variant={variant} type={type}>
      {children}
    </Button>
  );
};
export default AppButton;
