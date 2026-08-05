import { useEffect, useState } from "react";
import api from "../api/axios";

export default function Notifications() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState("");

  function load() {
    api
      .get("/notifications")
      .then((res) => setItems(res.data.data))
      .catch(() => setError("Could not load notifications."));
  }

  useEffect(load, []);

  async function markRead(id) {
    try {
      await api.put(`/notifications/read/${id}`);
      load();
    } catch {
      setError("Could not mark as read.");
    }
  }

  async function markAllRead() {
    try {
      await api.put("/notifications/read-all");
      load();
    } catch {
      setError("Could not mark all as read.");
    }
  }

  return (
    <div className="page">
      <h2>Notifications</h2>
      {error && <div className="error-box">{error}</div>}

      <button onClick={markAllRead} style={{ marginBottom: "1rem" }}>
        Mark all as read
      </button>

      <div className="notice-list">
        {items.map((n) => (
          <div
            className={`notice-card ${n.isRead ? "" : "unread"}`}
            key={n.notificationId}
          >
            <p>{n.message}</p>
            <div className="notice-meta">
              {n.type} • {n.createdAt ? new Date(n.createdAt).toLocaleString() : ""}
            </div>
            {!n.isRead && (
              <button onClick={() => markRead(n.notificationId)}>Mark as read</button>
            )}
          </div>
        ))}
        {items.length === 0 && <p>No notifications.</p>}
      </div>
    </div>
  );
}
