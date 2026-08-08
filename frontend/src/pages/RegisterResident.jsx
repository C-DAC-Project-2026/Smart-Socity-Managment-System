import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios";
import Captcha from "../components/Captcha";

// PUBLIC: a prospective Resident or Staff member selects their society by
// name from the ACTIVE societies list, then submits their details. The
// account is created INACTIVE and only works once their Society Admin
// approves it (see Approvals page).
export default function RegisterResident() {
  const [societies, setSocieties] = useState([]);
  const [societiesError, setSocietiesError] = useState("");
  const [form, setForm] = useState({
    name: "", email: "", password: "", role: "ROLE_RESIDENT",
    societyId: "", address: "", mobile: "", flatNo: "", department: "",
  });
  const [captcha, setCaptcha] = useState({ captchaId: "", answer: "" });
  const [captchaKey, setCaptchaKey] = useState(0);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api.get("/public/societies")
      .then((res) => setSocieties(res.data.data))
      .catch(() => setSocietiesError("Could not load the society list. Please refresh the page."));
  }, []);

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);
    try {
      const res = await api.post("/auth/register-public", {
        ...form,
        societyId: Number(form.societyId),
        captchaId: captcha.captchaId,
        captchaAnswer: captcha.answer,
      });
      setSuccess(res.data.data || res.data.message || "Registration submitted.");
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed. Please try again.");
      // Captcha is single-use server-side; refresh it or the next retry
      // will fail on "Incorrect captcha answer" even with valid input.
      setCaptchaKey((k) => k + 1);
    } finally {
      setLoading(false);
    }
  }

  if (success) {
    return (
      <div className="auth-page">
        <div className="auth-card">
          <h2>Request submitted 🎉</h2>
          <div className="success-box">{success}</div>
          <p className="muted">
            Your society's admin will review your request. You'll be able to log in with{" "}
            <strong>{form.email}</strong> once approved.
          </p>
          <Link to="/login"><button type="button">Back to Login</button></Link>
        </div>
      </div>
    );
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h2>Join your Society</h2>
        <p className="muted center">Select your society, then request access as a resident or staff member.</p>
        {error && <div className="error-box">{error}</div>}
        {societiesError && <div className="error-box">{societiesError}</div>}

        <label>Society</label>
        <select value={form.societyId} onChange={(e) => update("societyId", e.target.value)} required>
          <option value="" disabled>Select your society…</option>
          {societies.map((s) => (
            <option key={s.societyId} value={s.societyId}>
              {s.name} {s.city ? `— ${s.city}` : ""} ({s.societyCode})
            </option>
          ))}
        </select>
        {societies.length === 0 && !societiesError && (
          <p className="muted">Loading societies…</p>
        )}

        <label>I am a</label>
        <select value={form.role} onChange={(e) => update("role", e.target.value)}>
          <option value="ROLE_RESIDENT">Resident</option>
          <option value="ROLE_STAFF">Staff</option>
        </select>

        <label>Full Name</label>
        <input value={form.name} onChange={(e) => update("name", e.target.value)} required />

        <label>Email</label>
        <input type="email" value={form.email} onChange={(e) => update("email", e.target.value)} required />

        <label>Password</label>
        <input type="password" value={form.password} onChange={(e) => update("password", e.target.value)} required minLength={6} />

        {form.role === "ROLE_RESIDENT" ? (
          <>
            <label>Flat No</label>
            <input value={form.flatNo} onChange={(e) => update("flatNo", e.target.value)} required />
            <label>Address</label>
            <input value={form.address} onChange={(e) => update("address", e.target.value)} />
            <label>Mobile</label>
            <input value={form.mobile} onChange={(e) => update("mobile", e.target.value)} placeholder="10-digit mobile number" />
          </>
        ) : (
          <>
            <label>Department</label>
            <input value={form.department} onChange={(e) => update("department", e.target.value)}
              placeholder="e.g. Security, Housekeeping" required />
            <label>Mobile</label>
            <input value={form.mobile} onChange={(e) => update("mobile", e.target.value)} placeholder="10-digit mobile number" />
          </>
        )}

        <Captcha key={captchaKey} onChange={setCaptcha} />

        <button type="submit" disabled={loading || !form.societyId}>
          {loading ? "Submitting..." : "Request Access"}
        </button>
        <p className="auth-switch">
          Managing a society instead? <Link to="/register/society">Register your Society</Link>
        </p>
      </form>
    </div>
  );
}
