import { useEffect, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

export default function Staff() {
  const { user } = useAuth();
  const [staff, setStaff] = useState([]);
  const [error, setError] = useState("");
  const [editing, setEditing] = useState(null);

  function load() {
    if (user.role === "ROLE_ADMIN") {
      api
        .get("/staff")
        .then((res) => setStaff(res.data.data))
        .catch(() => setError("Could not load staff."));
    } else if (user.role === "ROLE_STAFF") {
      api
        .get(`/staff/user/${user.userId}`)
        .then((res) => setStaff([res.data.data]))
        .catch(() => setError("Could not load your staff profile."));
    }
  }

  useEffect(load, [user]);

  async function handleDelete(id) {
    if (!confirm("Delete this staff member?")) return;
    try {
      await api.delete(`/staff/${id}`);
      load();
    } catch {
      setError("Delete failed.");
    }
  }

  async function handleSave(e) {
    e.preventDefault();
    try {
      await api.put(`/staff/${editing.staffId}`, editing);
      setEditing(null);
      load();
    } catch {
      setError("Update failed.");
    }
  }

  return (
    <div className="page">
      <h2>Staff</h2>
      {error && <div className="error-box">{error}</div>}

      <table className="data-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Department</th>
            <th>Mobile</th>
            {user.role === "ROLE_ADMIN" && <th>Actions</th>}
          </tr>
        </thead>
        <tbody>
          {staff.map((s) => (
            <tr key={s.staffId}>
              <td>{s.name}</td>
              <td>{s.email}</td>
              <td>{s.department}</td>
              <td>{s.mobile}</td>
              {(user.role === "ROLE_ADMIN" || user.role === "ROLE_STAFF") && (
                <td>
                  <button onClick={() => setEditing(s)}>Edit</button>
                  {user.role === "ROLE_ADMIN" && (
                    <button className="danger" onClick={() => handleDelete(s.staffId)}>
                      Delete
                    </button>
                  )}
                </td>
              )}
            </tr>
          ))}
          {staff.length === 0 && (
            <tr>
              <td colSpan={5}>No staff found.</td>
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
            <h3>Edit Staff</h3>
            <label>Department</label>
            <input
              value={editing.department}
              onChange={(e) => setEditing({ ...editing, department: e.target.value })}
            />
            <label>Mobile</label>
            <input
              value={editing.mobile}
              onChange={(e) => setEditing({ ...editing, mobile: e.target.value })}
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
