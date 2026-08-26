import { Card } from "@mui/material";
import type { Theme } from "@mui/material/styles";
import type { SxProps } from "@mui/material/styles";
import type React from "react";

type AppCardProps = {
  children: React.ReactNode;
  variant?: "outlined" | "elevation";
  sx?: SxProps<Theme>;
};

const AppCard = ({ children, variant, sx }: AppCardProps) => {
  return (
    <Card sx={sx} variant={variant}>
      {children}
    </Card>
  );
};
export default AppCard;
