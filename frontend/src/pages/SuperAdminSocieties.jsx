import { useEffect, useState } from "react";
import api from "../api/axios";

// SUPER_ADMIN only. Registers new societies (creating the society's first
// Admin at the same time) and controls each society's PENDING/ACTIVE/
// SUSPENDED lifecycle. Suspending a society blocks every one of its users
// from logging in on their very next request.
export default function SuperAdminSocieties() {
  const [societies, setSocieties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    name: "", societyCode: "", address: "", city: "", state: "", pincode: "",
    contactEmail: "", contactPhone: "", adminName: "", adminEmail: "", adminPassword: "",
  });
  const [submitting, setSubmitting] = useState(false);

  function load() {
    setLoading(true);
    api
      .get("/super-admin/societies")
      .then((res) => setSocieties(res.data.data))
      .catch((err) => setError(err.response?.data?.message || "Failed to load societies."))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
  }

  async function handleCreate(e) {
    e.preventDefault();
    setSubmitting(true);
    setError("");
    try {
      await api.post("/super-admin/societies", form);
      setShowForm(false);
      setForm({ name: "", societyCode: "", address: "", city: "", state: "", pincode: "",
        contactEmail: "", contactPhone: "", adminName: "", adminEmail: "", adminPassword: "" });
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Failed to register society.");
    } finally {
      setSubmitting(false);
    }
  }

  async function activate(id) {
    try {
      await api.put(`/super-admin/societies/${id}/activate`);
      load();
    } catch {
      setError("Failed to activate society.");
    }
  }

  async function suspend(id) {
    try {
      await api.put(`/super-admin/societies/${id}/suspend`);
      load();
    } catch {
      setError("Failed to suspend society.");
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h2>Societies</h2>
        <button onClick={() => setShowForm((s) => !s)}>
          {showForm ? "Cancel" : "Add Society Directly"}
        </button>
      </div>
      <p className="muted">
        Societies registered publicly at /register/society also land here as PENDING —
        review and Activate them below to let their admin (and everyone else) log in.
      </p>

      {error && <div className="error-box">{error}</div>}

      {showForm && (
        <form className="card-form" onSubmit={handleCreate}>
          <h3>New Society</h3>
          <div className="two-col">
            <div>
              <label>Society Name</label>
              <input value={form.name} onChange={(e) => update("name", e.target.value)} required />
              <label>Society Code</label>
              <input value={form.societyCode} onChange={(e) => update("societyCode", e.target.value)} required />
              <label>Address</label>
              <input value={form.address} onChange={(e) => update("address", e.target.value)} required />
              <label>City</label>
              <input value={form.city} onChange={(e) => update("city", e.target.value)} />
              <label>State</label>
              <input value={form.state} onChange={(e) => update("state", e.target.value)} />
              <label>Pincode</label>
              <input value={form.pincode} onChange={(e) => update("pincode", e.target.value)} />
              <label>Contact Email</label>
              <input type="email" value={form.contactEmail} onChange={(e) => update("contactEmail", e.target.value)} required />
              <label>Contact Phone</label>
              <input value={form.contactPhone} onChange={(e) => update("contactPhone", e.target.value)} />
            </div>
            <div>
              <h3>First Society Admin</h3>
              <label>Admin Name</label>
              <input value={form.adminName} onChange={(e) => update("adminName", e.target.value)} required />
              <label>Admin Email</label>
              <input type="email" value={form.adminEmail} onChange={(e) => update("adminEmail", e.target.value)} required />
              <label>Admin Temporary Password</label>
              <input type="password" value={form.adminPassword} onChange={(e) => update("adminPassword", e.target.value)} required minLength={6} />
            </div>
          </div>
          <button type="submit" disabled={submitting}>
            {submitting ? "Registering..." : "Register Society"}
          </button>
        </form>
      )}

      {loading ? (
        <p>Loading...</p>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>Code</th><th>Name</th><th>City</th><th>Status</th><th>Contact Email</th><th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {societies.map((s) => (
              <tr key={s.societyId}>
                <td>{s.societyCode}</td>
                <td>{s.name}</td>
                <td>{s.city}</td>
                <td><span className={`badge badge-${s.status.toLowerCase()}`}>{s.status}</span></td>
                <td>{s.contactEmail}</td>
                <td>
                  {s.status !== "ACTIVE" && <button onClick={() => activate(s.societyId)}>Activate</button>}
                  {s.status !== "SUSPENDED" && <button className="danger" onClick={() => suspend(s.societyId)}>Suspend</button>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
