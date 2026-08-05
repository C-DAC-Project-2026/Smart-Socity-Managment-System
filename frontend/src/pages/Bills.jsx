import { useEffect, useState } from "react";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";

export default function Bills() {
  const { user } = useAuth();
  const [bills, setBills] = useState([]);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const [form, setForm] = useState({
    residentId: "",
    amount: "",
    dueDate: "",
    month: "",
    year: "",
  });

  const [genForm, setGenForm] = useState({ month: "", year: "", amount: "" });

  async function load() {
    setError("");
    try {
      if (user.role === "ROLE_ADMIN") {
        const res = await api.get("/bills", { params: { page: 0, size: 50 } });
        setBills(res.data.data.content);
      } else if (user.role === "ROLE_RESIDENT") {
        const residentRes = await api.get(`/residents/user/${user.userId}`);
        const residentId = residentRes.data.data.residentId;
        const res = await api.get(`/bills/resident/${residentId}`, {
          params: { page: 0, size: 50 },
        });
        setBills(res.data.data.content);
      }
    } catch {
      setError("Could not load bills.");
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  async function handleCreate(e) {
    e.preventDefault();
    try {
      await api.post("/bills", {
        residentId: Number(form.residentId),
        amount: Number(form.amount),
        dueDate: form.dueDate,
        month: Number(form.month),
        year: Number(form.year),
      });
      setForm({ residentId: "", amount: "", dueDate: "", month: "", year: "" });
      setMessage("Bill created.");
      load();
    } catch {
      setError("Could not create bill.");
    }
  }

  async function handleGenerateAll(e) {
    e.preventDefault();
    try {
      const res = await api.post("/bills/generate-all", null, {
        params: {
          month: Number(genForm.month),
          year: Number(genForm.year),
          amount: Number(genForm.amount),
        },
      });
      setMessage(res.data.data);
      load();
    } catch {
      setError("Could not generate bills.");
    }
  }

  async function handlePay(bill) {
    setError("");
    if (!window.Razorpay) {
      setError("Razorpay SDK not loaded. Check that the checkout.js script tag is in index.html.");
      return;
    }
    try {
      const orderRes = await api.post("/razorpay/create-order", {
        billId: bill.billId,
      });
      const { orderId, amount, currency, keyId, residentName, description } =
        orderRes.data.data;

      const options = {
        key: keyId,
        amount,
        currency,
        order_id: orderId,
        name: "Society Maintenance",
        description: description || `Bill for ${bill.month}/${bill.year}`,
        prefill: { name: residentName },
        theme: { color: "#1a237e" },
        handler: async (response) => {
          try {
            await api.post("/razorpay/verify-payment", {
              billId: bill.billId,
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
              paymentMode: "ONLINE",
            });
            setMessage("Payment successful.");
            load();
          } catch {
            setError("Payment was made but verification failed. Contact admin.");
          }
        },
        modal: {
          ondismiss: () => setError("Payment cancelled."),
        },
      };

      const rzp = new window.Razorpay(options);
      rzp.on("payment.failed", () => setError("Payment failed."));
      rzp.open();
    } catch {
      setError("Could not initiate payment.");
    }
  }

  return (
    <div className="page">
      <h2>Maintenance Bills</h2>
      {error && <div className="error-box">{error}</div>}
      {message && <div className="success-box">{message}</div>}

      {user.role === "ROLE_ADMIN" && (
        <div className="two-col">
          <form className="card-form" onSubmit={handleCreate}>
            <h3>Create Bill</h3>
            <label>Resident ID</label>
            <input
              value={form.residentId}
              onChange={(e) => setForm({ ...form, residentId: e.target.value })}
              required
            />
            <label>Amount</label>
            <input
              type="number"
              value={form.amount}
              onChange={(e) => setForm({ ...form, amount: e.target.value })}
              required
            />
            <label>Due Date</label>
            <input
              type="date"
              value={form.dueDate}
              onChange={(e) => setForm({ ...form, dueDate: e.target.value })}
              required
            />
            <label>Month (1-12)</label>
            <input
              type="number"
              min={1}
              max={12}
              value={form.month}
              onChange={(e) => setForm({ ...form, month: e.target.value })}
              required
            />
            <label>Year</label>
            <input
              type="number"
              value={form.year}
              onChange={(e) => setForm({ ...form, year: e.target.value })}
              required
            />
            <button type="submit">Create Bill</button>
          </form>

          <form className="card-form" onSubmit={handleGenerateAll}>
            <h3>Generate Bills for All Residents</h3>
            <label>Month (1-12)</label>
            <input
              type="number"
              min={1}
              max={12}
              value={genForm.month}
              onChange={(e) => setGenForm({ ...genForm, month: e.target.value })}
              required
            />
            <label>Year</label>
            <input
              type="number"
              value={genForm.year}
              onChange={(e) => setGenForm({ ...genForm, year: e.target.value })}
              required
            />
            <label>Amount</label>
            <input
              type="number"
              value={genForm.amount}
              onChange={(e) => setGenForm({ ...genForm, amount: e.target.value })}
              required
            />
            <button type="submit">Generate for All</button>
          </form>
        </div>
      )}

      <table className="data-table">
        <thead>
          <tr>
            <th>Month/Year</th>
            <th>Amount</th>
            <th>Due Date</th>
            <th>Status</th>
            <th>Resident</th>
            <th>Flat</th>
            {user.role === "ROLE_RESIDENT" && <th>Action</th>}
          </tr>
        </thead>
        <tbody>
          {bills.map((b) => (
            <tr key={b.billId}>
              <td>{b.month}/{b.year}</td>
              <td>₹{b.amount}</td>
              <td>{b.dueDate}</td>
              <td><span className={`badge badge-${b.status?.toLowerCase()}`}>{b.status}</span></td>
              <td>{b.residentName}</td>
              <td>{b.flatNo}</td>
              {user.role === "ROLE_RESIDENT" && (
                <td>
                  {!b.paid ? (
                    <button onClick={() => handlePay(b)}>Pay Now</button>
                  ) : (
                    "Paid"
                  )}
                </td>
              )}
            </tr>
          ))}
          {bills.length === 0 && (
            <tr>
              <td colSpan={7}>No bills found.</td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}