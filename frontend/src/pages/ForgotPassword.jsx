import { useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios";
import Captcha from "../components/Captcha";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [captcha, setCaptcha] = useState({ captchaId: "", answer: "" });
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
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
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

        <Captcha onChange={setCaptcha} />

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
