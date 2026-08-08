import { useEffect, useState } from "react";
import api from "../api/axios";

// Society Admin only. Residents/Staff who self-registered and picked this
// admin's society show up here until approved or rejected.
export default function Approvals() {
  const [residents, setResidents] = useState([]);
  const [staff, setStaff] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);

  function load() {
    setLoading(true);
    Promise.all([
      api.get("/residents/pending"),
      api.get("/staff/pending"),
    ])
      .then(([r, s]) => {
        setResidents(r.data.data);
        setStaff(s.data.data);
      })
      .catch((err) => setError(err.response?.data?.message || "Failed to load pending approvals."))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  async function approve(kind, id) {
    setBusyId(`${kind}-${id}`);
    try {
      await api.put(`/${kind}/${id}/approve`);
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Approval failed.");
    } finally {
      setBusyId(null);
    }
  }

  async function reject(kind, id) {
    if (!confirm("Reject and remove this registration request?")) return;
    setBusyId(`${kind}-${id}`);
    try {
      await api.put(`/${kind}/${id}/reject`);
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Rejection failed.");
    } finally {
      setBusyId(null);
    }
  }

  const totalPending = residents.length + staff.length;

  return (
    <div className="page">
      <div className="page-header">
        <h2>Pending Approvals</h2>
        {totalPending > 0 && <span className="badge badge-pending">{totalPending} waiting</span>}
      </div>
      <p className="muted">Residents and staff who self-registered and chose your society.</p>

      {error && <div className="error-box">{error}</div>}

      {loading ? (
        <p>Loading...</p>
      ) : totalPending === 0 ? (
        <div className="card-form">
          <p style={{ margin: 0 }}>🎉 No pending requests right now.</p>
        </div>
      ) : (
        <>
          {residents.length > 0 && (
            <>
              <h3>Residents ({residents.length})</h3>
              <table className="data-table">
                <thead>
                  <tr><th>Name</th><th>Email</th><th>Flat No</th><th>Mobile</th><th>Actions</th></tr>
                </thead>
                <tbody>
                  {residents.map((r) => (
                    <tr key={r.residentId}>
                      <td>{r.name}</td>
                      <td>{r.email}</td>
                      <td>{r.flatNo}</td>
                      <td>{r.mobile}</td>
                      <td>
                        <button disabled={busyId === `residents-${r.residentId}`}
                          onClick={() => approve("residents", r.residentId)}>Approve</button>
                        <button className="danger" disabled={busyId === `residents-${r.residentId}`}
                          onClick={() => reject("residents", r.residentId)}>Reject</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </>
          )}

          {staff.length > 0 && (
            <>
              <h3>Staff ({staff.length})</h3>
              <table className="data-table">
                <thead>
                  <tr><th>Name</th><th>Email</th><th>Department</th><th>Mobile</th><th>Actions</th></tr>
                </thead>
                <tbody>
                  {staff.map((s) => (
                    <tr key={s.staffId}>
                      <td>{s.name}</td>
                      <td>{s.email}</td>
                      <td>{s.department}</td>
                      <td>{s.mobile}</td>
                      <td>
                        <button disabled={busyId === `staff-${s.staffId}`}
                          onClick={() => approve("staff", s.staffId)}>Approve</button>
                        <button className="danger" disabled={busyId === `staff-${s.staffId}`}
                          onClick={() => reject("staff", s.staffId)}>Reject</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </>
          )}
        </>
      )}
    </div>
  );
}
