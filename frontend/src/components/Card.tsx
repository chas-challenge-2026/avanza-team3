import type React from "react";

type CardProps = {
  children: React.ReactNode;
};

const Card = ({ children }: CardProps) => {
  return (
    <div
      style={{
        border: "1px solid #ccc",
        backgroundColor: "var(bg-container)",
        borderRadius: "8px",
        padding: " 16px",
        maxWidth: "300px",
        margin: "10px"
      }}
    >
      {children}
    </div>
  );
};
export default Card;
