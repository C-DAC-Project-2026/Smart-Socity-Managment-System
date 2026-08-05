import { useEffect, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

export default function Dashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (user.role === "ROLE_ADMIN") {
      api
        .get("/dashboard/stats")
        .then((res) => setStats(res.data.data))
        .catch(() => setError("Could not load dashboard stats."));
    }
  }, [user]);

  return (
    <div className="page">
      <h2>Welcome, {user.name}</h2>
      <p>Role: {user.role.replace("ROLE_", "")}</p>

      {user.role === "ROLE_ADMIN" && (
        <>
          {error && <div className="error-box">{error}</div>}
          {!stats && !error && <p>Loading stats...</p>}
          {stats && (
            <div className="stats-grid">
              <StatCard label="Total Residents" value={stats.totalResidents} />
              <StatCard label="Total Staff" value={stats.totalStaff} />
              <StatCard label="Total Complaints" value={stats.totalComplaints} />
              <StatCard label="Pending Complaints" value={stats.pendingComplaints} />
              <StatCard label="Assigned Complaints" value={stats.assignedComplaints} />
              <StatCard label="In-Progress Complaints" value={stats.inProgressComplaints} />
              <StatCard label="Resolved Complaints" value={stats.resolvedComplaints} />
              <StatCard label="Total Bills" value={stats.totalBills} />
              <StatCard label="Paid Bills" value={stats.paidBills} />
              <StatCard label="Pending Bills" value={stats.pendingBills} />
              <StatCard label="Overdue Bills" value={stats.overdueBills} />
              <StatCard label="Total Payments" value={stats.totalPayments} />
            </div>
          )}
        </>
      )}

      {user.role !== "ROLE_ADMIN" && (
        <p>Use the navigation bar above to view complaints, notices, bills and payments.</p>
      )}
    </div>
  );
}

function StatCard({ label, value }) {
  return (
    <div className="stat-card">
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  );
}
