import { useEffect, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

export default function Notices() {
  const { user } = useAuth();
  const [notices, setNotices] = useState([]);
  const [error, setError] = useState("");
  const [form, setForm] = useState({ title: "", content: "" });
  const [editing, setEditing] = useState(null);

  function load() {
    api
      .get("/notices", { params: { page: 0, size: 50 } })
      .then((res) => setNotices(res.data.data.content))
      .catch(() => setError("Could not load notices."));
  }

  useEffect(load, []);

  async function handleCreate(e) {
    e.preventDefault();
    try {
      await api.post("/notices", form);
      setForm({ title: "", content: "" });
      load();
    } catch {
      setError("Could not create notice.");
    }
  }

  async function handleSave(e) {
    e.preventDefault();
    try {
      await api.put(`/notices/${editing.noticeId}`, editing);
      setEditing(null);
      load();
    } catch {
      setError("Update failed.");
    }
  }

  async function handleDelete(id) {
    if (!confirm("Delete this notice?")) return;
    try {
      await api.delete(`/notices/${id}`);
      load();
    } catch {
      setError("Delete failed.");
    }
  }

  return (
    <div className="page">
      <h2>Notices</h2>
      {error && <div className="error-box">{error}</div>}

      {user.role === "ROLE_ADMIN" && (
        <form className="card-form" onSubmit={handleCreate}>
          <h3>Publish a Notice</h3>
          <label>Title</label>
          <input
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            required
          />
          <label>Content</label>
          <textarea
            value={form.content}
            onChange={(e) => setForm({ ...form, content: e.target.value })}
            required
          />
          <button type="submit">Publish</button>
        </form>
      )}

      <div className="notice-list">
        {notices.map((n) => (
          <div className="notice-card" key={n.noticeId}>
            <h3>{n.title}</h3>
            <p>{n.content}</p>
            <div className="notice-meta">
              By {n.createdByName} • {n.createdAt ? new Date(n.createdAt).toLocaleString() : ""}
            </div>
            {user.role === "ROLE_ADMIN" && (
              <div className="notice-actions">
                <button onClick={() => setEditing(n)}>Edit</button>
                <button className="danger" onClick={() => handleDelete(n.noticeId)}>
                  Delete
                </button>
              </div>
            )}
          </div>
        ))}
        {notices.length === 0 && <p>No notices yet.</p>}
      </div>

      {editing && (
        <div className="modal-backdrop" onClick={() => setEditing(null)}>
          <form
            className="modal-card"
            onClick={(e) => e.stopPropagation()}
            onSubmit={handleSave}
          >
            <h3>Edit Notice</h3>
            <label>Title</label>
            <input
              value={editing.title}
              onChange={(e) => setEditing({ ...editing, title: e.target.value })}
            />
            <label>Content</label>
            <textarea
              value={editing.content}
              onChange={(e) => setEditing({ ...editing, content: e.target.value })}
            />
            <div className="modal-actions">
              <button type="submit">Save</button>
              <button type="button" onClick={() => setEditing(null)}>
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
