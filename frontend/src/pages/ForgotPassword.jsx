import { useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios";
import Captcha from "../components/Captcha";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [captcha, setCaptcha] = useState({ captchaId: "", answer: "" });
  const [captchaKey, setCaptchaKey] = useState(0);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);
    try {
      const res = await api.post("/auth/forgot-password", {
        email,
        captchaId: captcha.captchaId,
        captchaAnswer: captcha.answer,
      });
      setSuccess(res.data.message || "Reset link sent! Check your email.");
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Could not send reset link. Please try again."
      );
      // Captcha is single-use server-side; refresh it or the next retry
      // will fail on "Incorrect captcha answer" even with valid input.
      setCaptchaKey((k) => k + 1);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <Link to="/" className="brand-mark auth-page-brand">
        <span className="brand-mark-icon">SS</span>
        Smart Society
      </Link>
      <form className="auth-card" onSubmit={handleSubmit}>
        <h2>Forgot Password</h2>
        <p style={{ marginTop: "-0.5rem", color: "#6b7280", fontSize: "0.9rem" }}>
          Enter your account email and we'll send you a link to reset your password.
        </p>
        {error && <div className="error-box">{error}</div>}
        {success && <div className="success-box">{success}</div>}

        <label>Email</label>
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        <Captcha key={captchaKey} onChange={setCaptcha} />

        <button type="submit" disabled={loading}>
          {loading ? "Sending..." : "Send Reset Link"}
        </button>
        <p className="auth-switch">
          Remembered your password? <Link to="/login">Back to Login</Link>
        </p>
      </form>
    </div>
  );
}
