import { useEffect, useState, useCallback } from "react";
import api from "../api/axios";

/**
 * Usage:
 *   <Captcha onChange={({ captchaId, answer }) => ...} />
 *
 * Fetches a fresh math captcha on mount and whenever the refresh icon is
 * clicked. The parent form should include captchaId + answer in whatever
 * it submits (login / register / forgot-password all expect
 * captchaId + captchaAnswer fields).
 */
export default function Captcha({ onChange }) {
  const [question, setQuestion] = useState("");
  const [captchaId, setCaptchaId] = useState("");
  const [answer, setAnswer] = useState("");
  const [loading, setLoading] = useState(false);

  const fetchCaptcha = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get("/auth/captcha");
      const { captchaId: id, question: q } = res.data.data;
      setCaptchaId(id);
      setQuestion(q);
      setAnswer("");
      onChange({ captchaId: id, answer: "" });
    } catch {
      setQuestion("Could not load captcha - click refresh to retry");
    } finally {
      setLoading(false);
    }
  }, [onChange]);

  useEffect(() => {
    fetchCaptcha();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function handleAnswerChange(e) {
    const value = e.target.value;
    setAnswer(value);
    onChange({ captchaId, answer: value });
  }

  return (
    <div className="captcha-box">
      <label>Security check</label>
      <div className="captcha-row">
        <span className="captcha-question">{loading ? "Loading..." : question}</span>
        <button
          type="button"
          className="captcha-refresh"
          onClick={fetchCaptcha}
          title="Get a new question"
        >
          ↻
        </button>
      </div>
      <input
        type="text"
        value={answer}
        onChange={handleAnswerChange}
        placeholder="Your answer"
        required
        autoComplete="off"
      />
    </div>
  );
}
