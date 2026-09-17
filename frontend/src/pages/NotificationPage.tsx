import { useState, useEffect } from "react";
import "./NotificationPage.module.css";

type Notification = {
  id: string;
  message: string;
};

export default function NotificationPage() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const token = localStorage.getItem("token"); // eller varifrån ni nu hämtar den

  useEffect(() => {
    async function getNotifications() {
      const response = await fetch("/api/alerts", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error("Kunde inte hämta notifikationer");
      }

      const data = await response.json();
      setNotifications(data);
    }

    getNotifications();
  }, [token]);

  return (
    <div>
      <h1>Notifikationer</h1>
      {notifications.map((n) => (
        <p key={n.id}>{n.message}</p>
      ))}
    </div>
  );
}
