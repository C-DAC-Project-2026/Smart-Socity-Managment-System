import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  if (!user) return null;

  const role = user.role; // ROLE_ADMIN | ROLE_RESIDENT | ROLE_STAFF

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <nav className="navbar">
      <div className="navbar-brand">Smart Society</div>
      <div className="navbar-links">
        <Link to="/">Dashboard</Link>
        {role === "ROLE_ADMIN" && <Link to="/residents">Residents</Link>}
        {role === "ROLE_ADMIN" && <Link to="/staff">Staff</Link>}
        <Link to="/complaints">Complaints</Link>
        <Link to="/notices">Notices</Link>
        <Link to="/bills">Bills</Link>
        <Link to="/payments">Payments</Link>
        <Link to="/notifications">Notifications</Link>
      </div>
      <div className="navbar-user">
        <span>{user.name} ({role.replace("ROLE_", "")})</span>
        <button onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  );
}
