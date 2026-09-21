import { Button } from "@mui/material";
import type { Theme } from "@mui/material/styles";
import type { SxProps } from "@mui/material/styles";
import type { MouseEventHandler } from "react";

type AppButtonProps = {
  children: React.ReactNode;
  variant?: "outlined" | "contained";
  color?: "primary" | "secondary" | "error" | "success" | "warning";
  sx?: SxProps<Theme>;
  type?: "button" | "submit";
  disabled?: boolean;
  onClick?: MouseEventHandler<HTMLButtonElement>;
};

const AppButton = ({
  children,
  variant,
  color,
  sx,
  type,
  disabled,
  onClick
}: AppButtonProps) => {
  return (
    <Button
      sx={sx}
      variant={variant}
      color={color}
      type={type}
      disabled={disabled}
      onClick={onClick}
    >
      {children}
    </Button>
  );
};
export default AppButton;
