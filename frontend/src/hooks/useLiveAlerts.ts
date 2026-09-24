// hooks/useLiveAlerts.ts
import { useState, useEffect } from "react";
import type { LiveAlert } from "../types/notification";

function useLiveAlerts() {
  const [liveAlerts, setLiveAlerts] = useState<LiveAlert[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = localStorage.getItem("token");

    fetch("/api/alerts/live", {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => {
        if (!res.ok) throw new Error("Kunde inte hämta live-varningar");
        return res.json();
      })
      .then(setLiveAlerts)
      .catch(() => setError("Kunde inte hämta live-varningar"))
      .finally(() => setLoading(false));
  }, []);

  return { liveAlerts, loading, error };
}

export default useLiveAlerts;
