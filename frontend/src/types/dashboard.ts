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
  recentAlerts: Alert[];
};

export type Alert = {
  id: number;
  alertType: string;
  message: string;
  createdAt: string;
};
