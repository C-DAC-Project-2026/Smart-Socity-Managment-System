import { useState } from "react";
import { useAuth } from "../context/AuthContext";

// Society Admin only: adds a RESIDENT or STAFF user into the admin's own
// society. There is no public self-registration in a multi-tenant system —
// the backend derives societyId from the admin's JWT, never from this form.
export default function AddUser() {
  const { register } = useAuth();
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    role: "ROLE_RESIDENT",
    address: "",
    mobile: "",
    flatNo: "",
    department: "",
  });
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);
    try {
      const res = await register(form);
      setSuccess(res.message || "User registered successfully.");
      setForm((f) => ({ ...f, name: "", email: "", password: "", address: "", mobile: "", flatNo: "", department: "" }));
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed. Try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h2>Add Resident or Staff</h2>
        <p className="muted">New user is created in your society only.</p>
        {error && <div className="error-box">{error}</div>}
        {success && <div className="success-box">{success}</div>}

        <label>Name</label>
        <input value={form.name} onChange={(e) => update("name", e.target.value)} required />

        <label>Email</label>
        <input type="email" value={form.email} onChange={(e) => update("email", e.target.value)} required />

        <label>Temporary Password</label>
        <input type="password" value={form.password} onChange={(e) => update("password", e.target.value)} required minLength={6} />

        <label>Role</label>
        <select value={form.role} onChange={(e) => update("role", e.target.value)}>
          <option value="ROLE_RESIDENT">Resident</option>
          <option value="ROLE_STAFF">Staff</option>
        </select>

        {form.role === "ROLE_RESIDENT" && (
          <>
            <label>Address</label>
            <input value={form.address} onChange={(e) => update("address", e.target.value)} />
            <label>Flat No</label>
            <input value={form.flatNo} onChange={(e) => update("flatNo", e.target.value)} />
            <label>Mobile</label>
            <input value={form.mobile} onChange={(e) => update("mobile", e.target.value)} placeholder="10-digit mobile number" />
          </>
        )}

        {form.role === "ROLE_STAFF" && (
          <>
            <label>Department</label>
            <input value={form.department} onChange={(e) => update("department", e.target.value)} />
            <label>Mobile</label>
            <input value={form.mobile} onChange={(e) => update("mobile", e.target.value)} placeholder="10-digit mobile number" />
          </>
        )}

        <button type="submit" disabled={loading}>
          {loading ? "Adding..." : "Add User"}
        </button>
      </form>
    </div>
  );
}
