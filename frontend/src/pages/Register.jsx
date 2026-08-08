import { Link } from "react-router-dom";

export default function Register() {
  return (
    <div className="auth-page">
      <Link to="/" className="brand-mark auth-page-brand">
        <span className="brand-mark-icon">SS</span>
        Smart Society
      </Link>
      <div className="choice-card">
        <h2>How would you like to register?</h2>
        <p className="muted center">Pick the option that describes you.</p>

        <Link to="/register/society" className="choice-option">
          <div className="choice-icon">🏢</div>
          <div>
            <h3>I manage a society</h3>
            <p>Register your society and become its first Admin. Needs approval from the platform team.</p>
          </div>
          <span className="choice-arrow">→</span>
        </Link>

        <Link to="/register/user" className="choice-option">
          <div className="choice-icon">🙋</div>
          <div>
            <h3>I'm a Resident or Staff member</h3>
            <p>Select your society from the list and request access. Needs approval from your society admin.</p>
          </div>
          <span className="choice-arrow">→</span>
        </Link>

        <p className="auth-switch">
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </div>
    </div>
  );
}
