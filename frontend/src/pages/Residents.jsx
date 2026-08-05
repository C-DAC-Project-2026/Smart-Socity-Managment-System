import { useEffect, useState } from "react";
import api from "../api/axios";

export default function Residents() {
  const [residents, setResidents] = useState([]);
  const [query, setQuery] = useState("");
  const [error, setError] = useState("");
  const [editing, setEditing] = useState(null); // resident being edited

  function load() {
    api
      .get("/residents")
      .then((res) => setResidents(res.data.data))
      .catch(() => setError("Could not load residents."));
  }

  useEffect(load, []);

  async function handleSearch(e) {
    e.preventDefault();
    if (!query.trim()) return load();
    try {
      const res = await api.get("/residents/search", { params: { q: query } });
      setResidents(res.data.data);
    } catch {
      setError("Search failed.");
    }
  }

  async function handleDelete(id) {
    if (!confirm("Delete this resident?")) return;
    try {
      await api.delete(`/residents/${id}`);
      load();
    } catch {
      setError("Delete failed.");
    }
  }

  async function handleSave(e) {
    e.preventDefault();
    try {
      await api.put(`/residents/${editing.residentId}`, editing);
      setEditing(null);
      load();
    } catch {
      setError("Update failed.");
    }
  }

  return (
    <div className="page">
      <h2>Residents</h2>
      {error && <div className="error-box">{error}</div>}

      <form className="inline-form" onSubmit={handleSearch}>
        <input
          placeholder="Search by name / flat / email"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <button type="submit">Search</button>
        <button type="button" onClick={() => { setQuery(""); load(); }}>
          Reset
        </button>
      </form>

      <table className="data-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Flat No</th>
            <th>Mobile</th>
            <th>Address</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {residents.map((r) => (
            <tr key={r.residentId}>
              <td>{r.name}</td>
              <td>{r.email}</td>
              <td>{r.flatNo}</td>
              <td>{r.mobile}</td>
              <td>{r.address}</td>
              <td>
                <button onClick={() => setEditing(r)}>Edit</button>
                <button className="danger" onClick={() => handleDelete(r.residentId)}>
                  Delete
                </button>
              </td>
            </tr>
          ))}
          {residents.length === 0 && (
            <tr>
              <td colSpan={6}>No residents found.</td>
            </tr>
          )}
        </tbody>
      </table>

      {editing && (
        <div className="modal-backdrop" onClick={() => setEditing(null)}>
          <form
            className="modal-card"
            onClick={(e) => e.stopPropagation()}
            onSubmit={handleSave}
          >
            <h3>Edit Resident</h3>
            <label>Address</label>
            <input
              value={editing.address}
              onChange={(e) => setEditing({ ...editing, address: e.target.value })}
            />
            <label>Mobile</label>
            <input
              value={editing.mobile}
              onChange={(e) => setEditing({ ...editing, mobile: e.target.value })}
            />
            <label>Flat No</label>
            <input
              value={editing.flatNo}
              onChange={(e) => setEditing({ ...editing, flatNo: e.target.value })}
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
