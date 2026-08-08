import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  if (!user) return null;

  const role = user.role; // ROLE_SUPER_ADMIN | ROLE_ADMIN | ROLE_RESIDENT | ROLE_STAFF

  function handleLogout() {
    logout();
    navigate("/login");
  }

  const initials = (user.name || "?")
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((n) => n[0].toUpperCase())
    .join("");

  return (
    <nav className="navbar">
      <Link to={role === "ROLE_SUPER_ADMIN" ? "/super-admin/societies" : "/dashboard"} className="brand-mark navbar-brand">
        <span className="brand-mark-icon">SS</span>
        Smart Society
      </Link>
      <div className="navbar-links">
        <Link to={role === "ROLE_SUPER_ADMIN" ? "/super-admin/societies" : "/dashboard"} className="home-link">
          🏠 Home
        </Link>
        {role === "ROLE_SUPER_ADMIN" ? (
          <Link to="/super-admin/societies">Societies</Link>
        ) : (
          <>
            <Link to="/dashboard">Dashboard</Link>
            {role === "ROLE_ADMIN" && <Link to="/residents">Residents</Link>}
            {role === "ROLE_ADMIN" && <Link to="/staff">Staff</Link>}
            {role === "ROLE_ADMIN" && <Link to="/users/add">Add User</Link>}
            {role === "ROLE_ADMIN" && <Link to="/approvals">Approvals</Link>}
            <Link to="/complaints">Complaints</Link>
            <Link to="/notices">Notices</Link>
            <Link to="/bills">Bills</Link>
            <Link to="/payments">Payments</Link>
            <Link to="/notifications">Notifications</Link>
          </>
        )}
      </div>
      <div className="navbar-user">
        <span className="navbar-avatar">{initials}</span>
        <span>{user.name} ({role.replace("ROLE_", "")})</span>
        <button onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  );
}
