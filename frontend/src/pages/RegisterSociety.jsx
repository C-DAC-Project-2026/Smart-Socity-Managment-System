import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axios";
import Captcha from "../components/Captcha";

// PUBLIC: anyone can submit this. It creates the Society (status PENDING)
// and its first Admin account in one go. Nobody can log in until a platform
// Super Admin reviews and activates the society (see SuperAdminSocieties).
export default function RegisterSociety() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: "", societyCode: "", address: "", city: "", state: "", pincode: "",
    contactEmail: "", contactPhone: "", adminName: "", adminEmail: "", adminPassword: "",
  });
  const [captcha, setCaptcha] = useState({ captchaId: "", answer: "" });
  const [captchaKey, setCaptchaKey] = useState(0);
  const [error, setError] = useState("");
  const [submitted, setSubmitted] = useState(null);
  const [loading, setLoading] = useState(false);

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      const res = await api.post("/public/societies/register", {
        ...form,
        captchaId: captcha.captchaId,
        captchaAnswer: captcha.answer,
      });
      setSubmitted(res.data.data);
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed. Please try again.");
      // Captcha is single-use server-side; refresh it or the next retry
      // will fail on "Incorrect captcha answer" even with valid input.
      setCaptchaKey((k) => k + 1);
    } finally {
      setLoading(false);
    }
  }

  if (submitted) {
    return (
      <div className="auth-page">
        <div className="auth-card">
          <h2>Request submitted 🎉</h2>
          <div className="success-box">
            <strong>{submitted.name}</strong> ({submitted.societyCode}) has been submitted for review.
          </div>
          <p className="muted">
            A platform Super Admin needs to approve your society before you or anyone else can log
            in. You'll be able to sign in with <strong>{form.adminEmail}</strong> as soon as it's
            activated.
          </p>
          <Link to="/login"><button type="button">Back to Login</button></Link>
        </div>
      </div>
    );
  }

  return (
    <div className="auth-page">
      <form className="auth-card auth-card-wide" onSubmit={handleSubmit}>
        <h2>Register your Society</h2>
        <p className="muted center">This creates your society (pending approval) and your Admin login.</p>
        {error && <div className="error-box">{error}</div>}

        <div className="two-col">
          <div>
            <h3>Society details</h3>
            <label>Society Name</label>
            <input value={form.name} onChange={(e) => update("name", e.target.value)} required />
            <label>Society Code</label>
            <input value={form.societyCode} onChange={(e) => update("societyCode", e.target.value)}
              placeholder="e.g. GRNVW01" required />
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
            <h3>Your Admin account</h3>
            <label>Your Name</label>
            <input value={form.adminName} onChange={(e) => update("adminName", e.target.value)} required />
            <label>Your Email</label>
            <input type="email" value={form.adminEmail} onChange={(e) => update("adminEmail", e.target.value)} required />
            <label>Password</label>
            <input type="password" value={form.adminPassword} onChange={(e) => update("adminPassword", e.target.value)}
              required minLength={6} />

            <Captcha key={captchaKey} onChange={setCaptcha} />
          </div>
        </div>

        <button type="submit" disabled={loading}>
          {loading ? "Submitting..." : "Submit for Approval"}
        </button>
        <p className="auth-switch">
          Not a committee member? <Link to="/register/user">Register as Resident/Staff</Link>
        </p>
      </form>
    </div>
  );
}
