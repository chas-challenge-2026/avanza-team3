import { Card } from "@mui/material";
import type { Theme } from "@mui/material/styles";
import type { SxProps } from "@mui/material/styles";
import type React from "react";

type AppCardProps = {
  children: React.ReactNode;
  variant?: "outlined" | "elevation";
  sx?: SxProps<Theme>;
  width?: string | number;
};

const AppCard = ({ children, variant, sx, width }: AppCardProps) => {
  return (
    <Card
      sx={{
        padding: 2,
        marginBottom: 0,
        width,
        overflow: "visible",
        borderRadius: "12px",
        border: "1px solid var(--border)",
        boxShadow: "none",
        ...sx,
      }}
      variant={variant}
    >
      {children}
    </Card>
  );
};
export default AppCard;
