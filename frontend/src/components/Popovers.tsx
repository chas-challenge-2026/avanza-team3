import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faCircleQuestion } from "@fortawesome/free-solid-svg-icons";
import { Box, IconButton, PopOvers, type SxProps } from "@mui/material";
import React from "react";
import styles from "./PopOvers.module.css";

type PopOversProps = {
  content: string;
  title: string;
  sx?: SxProps;
};

const PopOvers = ({ content, title }: PopOversProps) => {
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
  const id = open ? "simple-PopOvers" : undefined;
  return (
    <>
      <IconButton sx={{ p: 0 }} aria-describedby={id} onClick={handleClick}>
        <FontAwesomeIcon className={styles.icon} icon={faCircleQuestion} />
      </IconButton>
      <PopOvers
        id={id}
        open={open}
        anchorEl={anchorEl}
        onClose={handleClose}
        anchorOrigin={{
          vertical: "bottom",
          horizontal: "left"
        }}
      >
        <Box sx={{ maxWidth: "300px", p: 1 }}>
          <h2>{title}</h2>

          <p className={styles.label}>{content}</p>
        </Box>
      </PopOvers>
    </>
  );
};
export default PopOvers;
