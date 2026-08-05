import { useEffect, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

const STATUSES = ["PENDING", "ASSIGNED", "IN_PROGRESS", "RESOLVED"];

export default function Complaints() {
  const { user } = useAuth();
  const [complaints, setComplaints] = useState([]);
  const [error, setError] = useState("");
  const [form, setForm] = useState({ title: "", description: "" });
  const [staffList, setStaffList] = useState([]);
  const [assignTarget, setAssignTarget] = useState({}); // complaintId -> staffId

  async function load() {
    setError("");
    try {
      if (user.role === "ROLE_ADMIN") {
        const res = await api.get("/complaints", { params: { page: 0, size: 50 } });
        setComplaints(res.data.data.content);
        const staffRes = await api.get("/staff");
        setStaffList(staffRes.data.data);
      } else if (user.role === "ROLE_STAFF") {
        const staffRes = await api.get(`/staff/user/${user.userId}`);
        const staffId = staffRes.data.data.staffId;
        const res = await api.get(`/complaints/staff/${staffId}`, {
          params: { page: 0, size: 50 },
        });
        setComplaints(res.data.data.content);
      } else if (user.role === "ROLE_RESIDENT") {
        const residentRes = await api.get(`/residents/user/${user.userId}`);
        const residentId = residentRes.data.data.residentId;
        const res = await api.get(`/complaints/resident/${residentId}`, {
          params: { page: 0, size: 50 },
        });
        setComplaints(res.data.data.content);
      }
    } catch {
      setError("Could not load complaints.");
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  async function handleCreate(e) {
    e.preventDefault();
    try {
      await api.post("/complaints", form);
      setForm({ title: "", description: "" });
      load();
    } catch {
      setError("Could not raise complaint.");
    }
  }

  async function handleStatusChange(id, status) {
    try {
      await api.put(`/complaints/status/${id}`, { status, remarks: "" });
      load();
    } catch {
      setError("Could not update status.");
    }
  }

  async function handleAssign(id) {
    const staffId = assignTarget[id];
    if (!staffId) return;
    try {
      await api.put(`/complaints/assign/${id}`, null, { params: { staffId } });
      load();
    } catch {
      setError("Could not assign complaint.");
    }
  }

  async function handleDelete(id) {
    if (!confirm("Delete this complaint?")) return;
    try {
      await api.delete(`/complaints/${id}`);
      load();
    } catch {
      setError("Delete failed.");
    }
  }

  return (
    <div className="page">
      <h2>Complaints</h2>
      {error && <div className="error-box">{error}</div>}

      {user.role === "ROLE_RESIDENT" && (
        <form className="card-form" onSubmit={handleCreate}>
          <h3>Raise a Complaint</h3>
          <label>Title</label>
          <input
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            required
          />
          <label>Description</label>
          <textarea
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
            required
          />
          <button type="submit">Submit Complaint</button>
        </form>
      )}

      <table className="data-table">
        <thead>
          <tr>
            <th>Title</th>
            <th>Description</th>
            <th>Status</th>
            <th>Resident</th>
            <th>Flat</th>
            <th>Assigned Staff</th>
            {user.role !== "ROLE_RESIDENT" && <th>Actions</th>}
            {user.role === "ROLE_ADMIN" && <th>Delete</th>}
          </tr>
        </thead>
        <tbody>
          {complaints.map((c) => (
            <tr key={c.complaintId}>
              <td>{c.title}</td>
              <td>{c.description}</td>
              <td><span className={`badge badge-${c.status?.toLowerCase()}`}>{c.status}</span></td>
              <td>{c.residentName}</td>
              <td>{c.flatNo}</td>
              <td>{c.assignedStaffName || "-"}</td>
              {(user.role === "ROLE_ADMIN" || user.role === "ROLE_STAFF") && (
                <td>
                  <select
                    value={c.status}
                    onChange={(e) => handleStatusChange(c.complaintId, e.target.value)}
                  >
                    {STATUSES.map((s) => (
                      <option key={s} value={s}>{s}</option>
                    ))}
                  </select>
                  {user.role === "ROLE_ADMIN" && (
                    <div className="assign-row">
                      <select
                        value={assignTarget[c.complaintId] || ""}
                        onChange={(e) =>
                          setAssignTarget({ ...assignTarget, [c.complaintId]: e.target.value })
                        }
                      >
                        <option value="">Assign staff...</option>
                        {staffList.map((s) => (
                          <option key={s.staffId} value={s.staffId}>{s.name}</option>
                        ))}
                      </select>
                      <button type="button" onClick={() => handleAssign(c.complaintId)}>
                        Assign
                      </button>
                    </div>
                  )}
                </td>
              )}
              {user.role === "ROLE_ADMIN" && (
                <td>
                  <button className="danger" onClick={() => handleDelete(c.complaintId)}>
                    Delete
                  </button>
                </td>
              )}
            </tr>
          ))}
          {complaints.length === 0 && (
            <tr>
              <td colSpan={8}>No complaints found.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
