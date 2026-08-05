import { useEffect, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

export default function Payments() {
  const { user } = useAuth();
  const [payments, setPayments] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      try {
        if (user.role === "ROLE_ADMIN") {
          const res = await api.get("/payments", { params: { page: 0, size: 50 } });
          setPayments(res.data.data.content);
        } else if (user.role === "ROLE_RESIDENT") {
          const residentRes = await api.get(`/residents/user/${user.userId}`);
          const residentId = residentRes.data.data.residentId;
          const res = await api.get(`/payments/resident/${residentId}`, {
            params: { page: 0, size: 50 },
          });
          setPayments(res.data.data.content);
        }
      } catch {
        setError("Could not load payments.");
      }
    }
    load();
  }, [user]);

  if (user.role === "ROLE_STAFF") {
    return (
      <div className="page">
        <h2>Payments</h2>
        <p>Payment records are only visible to Admins and Residents.</p>
      </div>
    );
  }

  return (
    <div className="page">
      <h2>Payments</h2>
      {error && <div className="error-box">{error}</div>}

      <table className="data-table">
        <thead>
          <tr>
            <th>Resident</th>
            <th>Flat</th>
            <th>Bill Period</th>
            <th>Amount</th>
            <th>Mode</th>
            <th>Status</th>
            <th>Transaction ID</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          {payments.map((p) => (
            <tr key={p.paymentId}>
              <td>{p.residentName}</td>
              <td>{p.flatNo}</td>
              <td>{p.billMonth}/{p.billYear}</td>
              <td>₹{p.amount}</td>
              <td>{p.paymentMode}</td>
              <td><span className={`badge badge-${p.status?.toLowerCase()}`}>{p.status}</span></td>
              <td>{p.transactionId || "-"}</td>
              <td>{p.paymentDate ? new Date(p.paymentDate).toLocaleString() : "-"}</td>
            </tr>
          ))}
          {payments.length === 0 && (
            <tr>
              <td colSpan={8}>No payments found.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
