export const accountRows = [
  {
    id: 1,
    account: "Anna ISK",
    type: "ISK",
    label: "ISK",
    value: "312 340",
    share: "43.8%",
  },
  {
    id: 2,
    account: "Anna KF",
    type: "KF",
    label: "KF",
    value: "201 230",
    share: "28.3%",
  },
  {
    id: 3,
    account: "Tjänstepension",
    type: "TJP",
    label: "TJP",
    value: "145 560",
    share: "20.4%",
  },
  {
    id: 4,
    account: "Depå",
    type: "Depa",
    label: "Depå",
    value: "53 437",
    share: "7,5%",
  },
];

export const accountColumns = [
  { field: "account", headerName: "Konto" },
  { field: "type", headerName: "Typ", isBadge: true },
  { field: "value", headerName: "Värde" },
  { field: "share", headerName: "Andel" },
];
