import { Button } from "@mui/material";
import type { Theme } from "@mui/material/styles";
import type { SxProps } from "@mui/material/styles";

type AppButtonProps = {
  children: React.ReactNode;
  variant?: "outlined" | "contained";
  sx?: SxProps<Theme>;
};

const AppButton = ({ children, variant, sx }: AppButtonProps) => {
  return (
    <Button sx={sx} variant={variant}>
      {children}
    </Button>
  );
};
export default AppButton;
