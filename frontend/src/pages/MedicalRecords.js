import React, { useEffect, useState, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import medicalRecordService from "../services/medicalRecordService";
import patientService from "../services/patientService";
import Loading from "../components/Loading";
import ErrorMessage from "../components/ErrorMessage";
import SuccessMessage from "../components/SuccessMessage";
import { extractErrorMessage } from "../utils/errorUtils";

/**
 * DOCTOR: creates records for a patient by id, and can browse a given patient's history.
 * PATIENT: sees only their own records (read-only) — enforced by the backend too.
 */
export default function MedicalRecords() {
  const { user } = useAuth();
  const [patientId, setPatientId] = useState("");
  const [records, setRecords] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ patientId: "", symptoms: "", diagnosis: "", treatmentNotes: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const loadOwn = useCallback(async () => {
    setLoading(true);
    try {
      const pat = await patientService.getMyProfile();
      const data = await medicalRecordService.getByPatient(pat.id);
      setRecords(data);
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
      const data = await medicalRecordService.getByPatient(patientId);
      setRecords(data);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setError(""); setSuccess("");
    try {
      await medicalRecordService.create({ ...form, patientId: Number(form.patientId) });
      setSuccess("Medical record created");
      setShowForm(false);
      if (String(form.patientId) === String(patientId)) handleLookup(e);
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div className="page-container">
      <div className="section-header">
        <h2>Medical Records</h2>
        {user.role === "DOCTOR" && (
          <button className="primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? "Cancel" : "+ New Record"}
          </button>
        )}
      </div>

      <ErrorMessage message={error} />
      <SuccessMessage message={success} />

      {showForm && (
        <div className="card">
          <h3>New Medical Record</h3>
          <form onSubmit={handleCreate}>
            <div><label>Patient ID</label><input required type="number" value={form.patientId} onChange={(e) => setForm({ ...form, patientId: e.target.value })} /></div>
            <div><label>Symptoms</label><textarea required rows={2} value={form.symptoms} onChange={(e) => setForm({ ...form, symptoms: e.target.value })} /></div>
            <div><label>Diagnosis</label><textarea required rows={2} value={form.diagnosis} onChange={(e) => setForm({ ...form, diagnosis: e.target.value })} /></div>
            <div><label>Treatment Notes</label><textarea rows={2} value={form.treatmentNotes} onChange={(e) => setForm({ ...form, treatmentNotes: e.target.value })} /></div>
            <button className="primary" type="submit">Save Record</button>
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
          <table>
            <thead><tr><th>Date</th><th>Doctor</th><th>Symptoms</th><th>Diagnosis</th><th>Treatment Notes</th></tr></thead>
            <tbody>
              {records.map((r) => (
                <tr key={r.id}>
                  <td>{new Date(r.createdAt).toLocaleDateString()}</td>
                  <td>{r.doctorName}</td>
                  <td>{r.symptoms}</td>
                  <td>{r.diagnosis}</td>
                  <td>{r.treatmentNotes || "—"}</td>
                </tr>
              ))}
              {records.length === 0 && <tr><td colSpan={5}>No records to show</td></tr>}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
