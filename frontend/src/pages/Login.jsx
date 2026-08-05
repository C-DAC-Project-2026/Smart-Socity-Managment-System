import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Captcha from "../components/Captcha";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [captcha, setCaptcha] = useState({ captchaId: "", answer: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login(email, password, captcha.captchaId, captcha.answer);
      navigate("/");
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Login failed. Check your email and password."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h2>Smart Society Login</h2>
        {error && <div className="error-box">{error}</div>}
        <label>Email</label>
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <label>Password</label>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          minLength={6}
        />
        <div className="auth-links-row">
          <Link to="/forgot-password">Forgot password?</Link>
        </div>

        <Captcha onChange={setCaptcha} />

        <button type="submit" disabled={loading}>
          {loading ? "Logging in..." : "Login"}
        </button>
        <p className="auth-switch">
          No account? <Link to="/register">Register here</Link>
        </p>
      </form>
    </div>
  );
}
