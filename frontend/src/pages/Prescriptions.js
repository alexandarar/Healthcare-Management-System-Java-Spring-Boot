import React, { useEffect, useState, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import prescriptionService from "../services/prescriptionService";
import patientService from "../services/patientService";
import Loading from "../components/Loading";
import ErrorMessage from "../components/ErrorMessage";
import SuccessMessage from "../components/SuccessMessage";
import { extractErrorMessage } from "../utils/errorUtils";

const emptyMedicine = { name: "", dosage: "", frequency: "", duration: "", instructions: "" };

export default function Prescriptions() {
  const { user } = useAuth();
  const [patientId, setPatientId] = useState("");
  const [prescriptions, setPrescriptions] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ patientId: "", generalInstructions: "" });
  const [medicines, setMedicines] = useState([{ ...emptyMedicine }]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const loadOwn = useCallback(async () => {
    setLoading(true);
    try {
      const pat = await patientService.getMyProfile();
      const data = await prescriptionService.getByPatient(pat.id);
      setPrescriptions(data);
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
      const data = await prescriptionService.getByPatient(patientId);
      setPrescriptions(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const updateMedicine = (idx, field, value) => {
    const next = [...medicines];
    next[idx] = { ...next[idx], [field]: value };
    setMedicines(next);
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setError(""); setSuccess("");
    try {
      await prescriptionService.create({
        patientId: Number(form.patientId),
        generalInstructions: form.generalInstructions,
        medicines,
      });
      setSuccess("Prescription created");
      setShowForm(false);
      setMedicines([{ ...emptyMedicine }]);
      if (String(form.patientId) === String(patientId)) handleLookup(e);
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div className="page-container">
      <div className="section-header">
        <h2>Prescriptions</h2>
        {user.role === "DOCTOR" && (
          <button className="primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? "Cancel" : "+ New Prescription"}
          </button>
        )}
      </div>

      <ErrorMessage message={error} />
      <SuccessMessage message={success} />

      {showForm && (
        <div className="card">
          <h3>New Prescription</h3>
          <form onSubmit={handleCreate}>
            <div><label>Patient ID</label><input required type="number" value={form.patientId} onChange={(e) => setForm({ ...form, patientId: e.target.value })} /></div>

            {medicines.map((m, idx) => (
              <div key={idx} className="card" style={{ background: "#f9fafb" }}>
                <label>Medicine {idx + 1}</label>
                <input placeholder="Name" required value={m.name} onChange={(e) => updateMedicine(idx, "name", e.target.value)} />
                <input placeholder="Dosage (e.g. 500mg)" required value={m.dosage} onChange={(e) => updateMedicine(idx, "dosage", e.target.value)} />
                <input placeholder="Frequency (e.g. Twice daily)" required value={m.frequency} onChange={(e) => updateMedicine(idx, "frequency", e.target.value)} />
                <input placeholder="Duration (e.g. 5 days)" required value={m.duration} onChange={(e) => updateMedicine(idx, "duration", e.target.value)} />
                <input placeholder="Instructions" value={m.instructions} onChange={(e) => updateMedicine(idx, "instructions", e.target.value)} />
              </div>
            ))}
            <button type="button" className="secondary" onClick={() => setMedicines([...medicines, { ...emptyMedicine }])}>+ Add another medicine</button>

            <div><label>General Instructions</label><textarea rows={2} value={form.generalInstructions} onChange={(e) => setForm({ ...form, generalInstructions: e.target.value })} /></div>
            <button className="primary" type="submit">Save Prescription</button>
          </form>
        </div>
      )}

      {user.role === "DOCTOR" && (
        <form className="search-bar" onSubmit={handleLookup}>
          <input placeholder="Patient ID to view history..." value={patientId} onChange={(e) => setPatientId(e.target.value)} />
          <button className="primary" type="submit">Lookup</button>
        </form>
      )}

      {loading ? <Loading /> : (
        <div className="card">
          {prescriptions.map((p) => (
            <div key={p.id} className="card" style={{ background: "#f9fafb" }}>
              <p><strong>Doctor:</strong> {p.doctorName} — <strong>Date:</strong> {new Date(p.createdAt).toLocaleDateString()}</p>
              <table>
                <thead><tr><th>Medicine</th><th>Dosage</th><th>Frequency</th><th>Duration</th><th>Instructions</th></tr></thead>
                <tbody>
                  {p.medicines.map((m, i) => (
                    <tr key={i}>
                      <td>{m.name}</td><td>{m.dosage}</td><td>{m.frequency}</td><td>{m.duration}</td><td>{m.instructions || "—"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {p.generalInstructions && <p style={{ marginTop: "0.5rem" }}><strong>Notes:</strong> {p.generalInstructions}</p>}
            </div>
          ))}
          {prescriptions.length === 0 && <p>No prescriptions to show</p>}
        </div>
      )}
    </div>
  );
}
