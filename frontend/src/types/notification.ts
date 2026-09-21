export type Notification = {
  id: number;
  alertType: string;
  message: string;
  dismissed: boolean;
  createdAt: string;
};

export type LiveAlert = {
  alert_type: string;
  message: string;
  dismissed: boolean;
  created_at: string;
};
