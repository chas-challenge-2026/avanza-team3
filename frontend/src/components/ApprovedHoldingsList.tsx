import type { Instrument } from "../types/Instrument";

type Props = {
  approved: Instrument[];
  onRemove: (id: number) => void;
};

const ApprovedHoldingsList = ({ approved, onRemove }: Props) => {
  return (
    <ul>
      {approved.map((i) => (
        <li key={i.id}>
          {i.ticker} - {i.name}
          <button onClick={() => onRemove(i.id)}>Ta bort</button>
        </li>
      ))}
    </ul>
  );
};

export default ApprovedHoldingsList;
