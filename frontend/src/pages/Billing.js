import React, { useEffect, useState, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import billingService from "../services/billingService";
import patientService from "../services/patientService";
import Loading from "../components/Loading";
import ErrorMessage from "../components/ErrorMessage";
import SuccessMessage from "../components/SuccessMessage";
import StatusBadge from "./shared/StatusBadge";
import { extractErrorMessage } from "../utils/errorUtils";

const emptyItem = { chargeType: "CONSULTATION", description: "", amount: "" };

export default function Billing() {
  const { user } = useAuth();
  const [patientId, setPatientId] = useState("");
  const [bills, setBills] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ patientId: "" });
  const [items, setItems] = useState([{ ...emptyItem }]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [payingBillId, setPayingBillId] = useState(null);
  const [paymentForm, setPaymentForm] = useState({ amount: "", method: "CASH", transactionReference: "" });

  const loadOwn = useCallback(async () => {
    setLoading(true);
    try {
      const pat = await patientService.getMyProfile();
      const data = await billingService.getByPatient(pat.id);
      setBills(data);
    } catch (e) {
      setError(extractErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (user.role === "PATIENT") loadOwn();
  }, [user.role, loadOwn]);

  const handleLookup = async (e) => {
    e.preventDefault();
    if (!patientId) return;
    setLoading(true); setError("");
    try {
      const data = await billingService.getByPatient(patientId);
      setBills(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const updateItem = (idx, field, value) => {
    const next = [...items];
    next[idx] = { ...next[idx], [field]: value };
    setItems(next);
  };

  const handleGenerate = async (e) => {
    e.preventDefault();
    setError(""); setSuccess("");
    try {
      await billingService.generateBill({
        patientId: Number(form.patientId),
        items: items.map((i) => ({ ...i, amount: Number(i.amount) })),
      });
      setSuccess("Bill generated");
      setShowForm(false);
      setItems([{ ...emptyItem }]);
      if (String(form.patientId) === String(patientId)) handleLookup(e);
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handlePay = async (billId) => {
    setError(""); setSuccess("");
    try {
      await billingService.recordPayment(billId, { ...paymentForm, amount: Number(paymentForm.amount) });
      setSuccess("Payment recorded");
      setPayingBillId(null);
      setPaymentForm({ amount: "", method: "CASH", transactionReference: "" });
      if (patientId) handleLookup({ preventDefault: () => {} });
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div className="page-container">
      <div className="section-header">
        <h2>Billing</h2>
        {(user.role === "ADMIN" || user.role === "RECEPTIONIST") && (
          <button className="primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? "Cancel" : "+ Generate Bill"}
          </button>
        )}
      </div>

      <ErrorMessage message={error} />
      <SuccessMessage message={success} />

      {showForm && (
        <div className="card">
          <h3>Generate Bill</h3>
          <form onSubmit={handleGenerate}>
            <div><label>Patient ID</label><input required type="number" value={form.patientId} onChange={(e) => setForm({ ...form, patientId: e.target.value })} /></div>
            {items.map((it, idx) => (
              <div key={idx} className="card" style={{ background: "#f9fafb" }}>
                <label>Charge {idx + 1}</label>
                <select value={it.chargeType} onChange={(e) => updateItem(idx, "chargeType", e.target.value)}>
                  <option value="CONSULTATION">Consultation</option>
                  <option value="MEDICINE">Medicine</option>
                  <option value="OTHER">Other</option>
                </select>
                <input placeholder="Description" required value={it.description} onChange={(e) => updateItem(idx, "description", e.target.value)} />
                <input placeholder="Amount" type="number" step="0.01" required value={it.amount} onChange={(e) => updateItem(idx, "amount", e.target.value)} />
              </div>
            ))}
            <button type="button" className="secondary" onClick={() => setItems([...items, { ...emptyItem }])}>+ Add another charge</button>
            <button className="primary" type="submit">Generate Bill</button>
          </form>
        </div>
      )}

      {(user.role === "ADMIN" || user.role === "RECEPTIONIST") && (
        <form className="search-bar" onSubmit={handleLookup}>
          <input placeholder="Patient ID to view bills..." value={patientId} onChange={(e) => setPatientId(e.target.value)} />
          <button className="primary" type="submit">Lookup</button>
        </form>
      )}

      {loading ? <Loading /> : (
        <div className="card">
          {bills.map((b) => (
            <div key={b.id} className="card" style={{ background: "#f9fafb" }}>
              <div className="section-header">
                <div>
                  <strong>{b.invoiceNumber}</strong> — <StatusBadge status={b.status} />
                </div>
                {(user.role === "ADMIN" || user.role === "RECEPTIONIST") && b.status !== "PAID" && (
                  <button className="secondary" onClick={() => setPayingBillId(payingBillId === b.id ? null : b.id)}>
                    {payingBillId === b.id ? "Cancel" : "Record Payment"}
                  </button>
                )}
              </div>
              <table>
                <thead><tr><th>Type</th><th>Description</th><th>Amount</th></tr></thead>
                <tbody>
                  {b.items.map((it, i) => (
                    <tr key={i}><td>{it.chargeType}</td><td>{it.description}</td><td>${Number(it.amount).toFixed(2)}</td></tr>
                  ))}
                </tbody>
              </table>
              <p style={{ marginTop: "0.5rem" }}>
                <strong>Total:</strong> ${Number(b.totalAmount).toFixed(2)} &nbsp;
                <strong>Paid:</strong> ${Number(b.amountPaid).toFixed(2)} &nbsp;
                <strong>Balance:</strong> ${Number(b.balanceDue).toFixed(2)}
              </p>

              {payingBillId === b.id && (
                <div className="card">
                  <div><label>Amount</label><input type="number" step="0.01" value={paymentForm.amount} onChange={(e) => setPaymentForm({ ...paymentForm, amount: e.target.value })} /></div>
                  <div>
                    <label>Method</label>
                    <select value={paymentForm.method} onChange={(e) => setPaymentForm({ ...paymentForm, method: e.target.value })}>
                      <option value="CASH">Cash</option>
                      <option value="CARD">Card</option>
                      <option value="UPI">UPI</option>
                      <option value="INSURANCE">Insurance</option>
                    </select>
                  </div>
                  <div><label>Reference (optional)</label><input value={paymentForm.transactionReference} onChange={(e) => setPaymentForm({ ...paymentForm, transactionReference: e.target.value })} /></div>
                  <button className="primary" onClick={() => handlePay(b.id)}>Confirm Payment</button>
                </div>
              )}
            </div>
          ))}
          {bills.length === 0 && <p>No bills to show</p>}
        </div>
      )}
    </div>
  );
}
