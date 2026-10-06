import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCircleQuestion } from "@fortawesome/free-solid-svg-icons";
import { Box, IconButton, Popover, type SxProps } from "@mui/material";
import React from "react";
import styles from "./Popover.module.css";

type PopoverProps = {
  content: string;
  title: string;
  sx?: SxProps;
};

const PopOver = ({ content, sx, title }: PopoverProps) => {
  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(
    null
  );

  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const open = Boolean(anchorEl);
  const id = open ? "simple-popover" : undefined;
  return (
    <>
      <IconButton sx={{ p: 0 }} aria-describedby={id} onClick={handleClick}>
        <FontAwesomeIcon className={styles.icon} icon={faCircleQuestion} />
      </IconButton>
      <Popover
        id={id}
        open={open}
        anchorEl={anchorEl}
        onClose={handleClose}
        anchorOrigin={{
          vertical: "bottom",
          horizontal: "left"
        }}
        sx={{ maxWidth: "600px" }}
      >
        <Box sx={{ padding: 1 }}>
          <h2>{title}</h2>

          <p className={styles.label}>{content}</p>
        </Box>
      </Popover>
    </>
  );
};
export default PopOver;
