export type Account = {
  id: number;
  userId: number;
  accountType: string;
  accountName: string;
  currency: string;
  totalValueSek: number;
};

export type DashboardResponse = {
  accounts: Account[];
};
