import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Captcha from "../components/Captcha";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
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
  const [captcha, setCaptcha] = useState({ captchaId: "", answer: "" });
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
      await register({
        ...form,
        captchaId: captcha.captchaId,
        captchaAnswer: captcha.answer,
      });
      setSuccess("Registration successful! You can now log in.");
      setTimeout(() => navigate("/login"), 1200);
    } catch (err) {
      setError(
        err.response?.data?.message || "Registration failed. Try again."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h2>Create Account</h2>
        {error && <div className="error-box">{error}</div>}
        {success && <div className="success-box">{success}</div>}

        <label>Name</label>
        <input
          value={form.name}
          onChange={(e) => update("name", e.target.value)}
          required
        />

        <label>Email</label>
        <input
          type="email"
          value={form.email}
          onChange={(e) => update("email", e.target.value)}
          required
        />

        <label>Password</label>
        <input
          type="password"
          value={form.password}
          onChange={(e) => update("password", e.target.value)}
          required
          minLength={6}
        />

        <label>Role</label>
        <select value={form.role} onChange={(e) => update("role", e.target.value)}>
          <option value="ROLE_RESIDENT">Resident</option>
          <option value="ROLE_STAFF">Staff</option>
        </select>

        {form.role === "ROLE_RESIDENT" && (
          <>
            <label>Address</label>
            <input
              value={form.address}
              onChange={(e) => update("address", e.target.value)}
            />
            <label>Flat No</label>
            <input
              value={form.flatNo}
              onChange={(e) => update("flatNo", e.target.value)}
            />
            <label>Mobile</label>
            <input
              value={form.mobile}
              onChange={(e) => update("mobile", e.target.value)}
              placeholder="10-digit mobile number"
            />
          </>
        )}

        {form.role === "ROLE_STAFF" && (
          <>
            <label>Department</label>
            <input
              value={form.department}
              onChange={(e) => update("department", e.target.value)}
            />
            <label>Mobile</label>
            <input
              value={form.mobile}
              onChange={(e) => update("mobile", e.target.value)}
              placeholder="10-digit mobile number"
            />
          </>
        )}

        <Captcha onChange={setCaptcha} />

        <button type="submit" disabled={loading}>
          {loading ? "Registering..." : "Register"}
        </button>
        <p className="auth-switch">
          Already have an account? <Link to="/login">Login here</Link>
        </p>
      </form>
    </div>
  );
}
