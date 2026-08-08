import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import api from "../api/axios";

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get("token") || "";

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSuccess("");

    if (!token) {
      setError("Missing or invalid reset link. Please request a new one.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setLoading(true);
    try {
      const res = await api.post("/auth/reset-password", { token, newPassword });
      setSuccess(res.data.message || "Password reset successful!");
      setTimeout(() => navigate("/login"), 1500);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Could not reset password. The link may have expired."
      );
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
        <h2>Reset Password</h2>
        {!token && (
          <div className="error-box">
            No reset token found in the link. Please use the link from your email,
            or request a new one from the Forgot Password page.
          </div>
        )}
        {error && <div className="error-box">{error}</div>}
        {success && <div className="success-box">{success}</div>}

        <label>New Password</label>
        <input
          type="password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          required
          minLength={6}
        />

        <label>Confirm New Password</label>
        <input
          type="password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          required
          minLength={6}
        />

        <button type="submit" disabled={loading || !token}>
          {loading ? "Resetting..." : "Reset Password"}
        </button>
        <p className="auth-switch">
          <Link to="/login">Back to Login</Link>
        </p>
      </form>
    </div>
  );
}
