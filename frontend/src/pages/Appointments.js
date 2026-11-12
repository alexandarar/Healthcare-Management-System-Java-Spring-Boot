import React, { useEffect, useState, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import appointmentService from "../services/appointmentService";
import patientService from "../services/patientService";
import doctorService from "../services/doctorService";
import Loading from "../components/Loading";
import ErrorMessage from "../components/ErrorMessage";
import SuccessMessage from "../components/SuccessMessage";
import StatusBadge from "./shared/StatusBadge";
import { extractErrorMessage } from "../utils/errorUtils";

/**
 * One page adapts to all four roles:
 * - ADMIN/RECEPTIONIST: see every appointment, can book for any patient/doctor.
 * - DOCTOR: sees only their own appointments, can update status.
 * - PATIENT: sees only their own, can book/reschedule/cancel.
 * The backend enforces the real restrictions regardless of what this page renders.
 */
export default function Appointments() {
  const { user } = useAuth();
  const [appointments, setAppointments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [myPatientId, setMyPatientId] = useState(null);
  const [myDoctorId, setMyDoctorId] = useState(null);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ patientId: "", doctorId: "", appointmentDateTime: "", reason: "" });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const loadAppointments = useCallback(async () => {
    setLoading(true);
    try {
      let data;
      if (user.role === "ADMIN" || user.role === "RECEPTIONIST") {
        data = await appointmentService.getAll();
      } else if (user.role === "DOCTOR") {
        const doc = await doctorService.getMyProfile();
        setMyDoctorId(doc.id);
        data = await appointmentService.getByDoctor(doc.id);
      } else {
        const pat = await patientService.getMyProfile();
        setMyPatientId(pat.id);
        data = await appointmentService.getByPatient(pat.id);
      }
      setAppointments(data);
    } catch (e) {
      setError(extractErrorMessage(e));
    } finally {
      setLoading(false);
    }
  }, [user.role]);

  useEffect(() => { loadAppointments(); }, [loadAppointments]);

  useEffect(() => {
    if (user.role === "ADMIN" || user.role === "RECEPTIONIST" || user.role === "PATIENT") {
      doctorService.getAll().then(setDoctors).catch(() => {});
    }
  }, [user.role]);

  const handleBook = async (e) => {
    e.preventDefault();
    setError(""); setSuccess("");
    try {
      const payload = {
        patientId: user.role === "PATIENT" ? myPatientId : Number(form.patientId),
        doctorId: Number(form.doctorId),
        appointmentDateTime: form.appointmentDateTime,
        reason: form.reason,
      };
      await appointmentService.book(payload);
      setSuccess("Appointment booked successfully");
      setShowForm(false);
      setForm({ patientId: "", doctorId: "", appointmentDateTime: "", reason: "" });
      loadAppointments();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handleCancel = async (id) => {
    if (!window.confirm("Cancel this appointment?")) return;
    try {
      await appointmentService.cancel(id);
      loadAppointments();
    } catch (e) {
      setError(extractErrorMessage(e));
    }
  };

  const handleStatusChange = async (id, status) => {
    try {
      await appointmentService.updateStatus(id, { status });
      loadAppointments();
    } catch (e) {
      setError(extractErrorMessage(e));
    }
  };

  const canBook = user.role !== "DOCTOR";

  return (
    <div className="page-container">
      <div className="section-header">
        <h2>Appointments</h2>
        {canBook && (
          <button className="primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? "Cancel" : "+ Book Appointment"}
          </button>
        )}
      </div>

      <ErrorMessage message={error} />
      <SuccessMessage message={success} />

      {showForm && (
        <div className="card">
          <h3>Book Appointment</h3>
          <form onSubmit={handleBook}>
            {user.role !== "PATIENT" && (
              <div>
                <label>Patient ID</label>
                <input required type="number" value={form.patientId} onChange={(e) => setForm({ ...form, patientId: e.target.value })} />
              </div>
            )}
            <div>
              <label>Doctor</label>
              <select required value={form.doctorId} onChange={(e) => setForm({ ...form, doctorId: e.target.value })}>
                <option value="">Select a doctor</option>
                {doctors.map((d) => (
                  <option key={d.id} value={d.id}>{d.fullName} — {d.specialization}</option>
                ))}
              </select>
            </div>
            <div>
              <label>Date &amp; Time</label>
              <input required type="datetime-local" value={form.appointmentDateTime} onChange={(e) => setForm({ ...form, appointmentDateTime: e.target.value })} />
            </div>
            <div>
              <label>Reason</label>
              <input required value={form.reason} onChange={(e) => setForm({ ...form, reason: e.target.value })} />
            </div>
            <button className="primary" type="submit">Book</button>
          </form>
        </div>
      )}

      {loading ? <Loading /> : (
        <div className="card">
          <table>
            <thead>
              <tr>
                <th>Patient</th><th>Doctor</th><th>Date/Time</th><th>Reason</th><th>Status</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {appointments.map((a) => (
                <tr key={a.id}>
                  <td>{a.patientName}</td>
                  <td>{a.doctorName}</td>
                  <td>{new Date(a.appointmentDateTime).toLocaleString()}</td>
                  <td>{a.reason}</td>
                  <td><StatusBadge status={a.status} /></td>
                  <td>
                    {user.role === "DOCTOR" && a.status !== "CANCELLED" && a.status !== "COMPLETED" && (
                      <select defaultValue="" onChange={(e) => e.target.value && handleStatusChange(a.id, e.target.value)}>
                        <option value="" disabled>Update status</option>
                        <option value="CONFIRMED">Confirm</option>
                        <option value="COMPLETED">Complete</option>
                        <option value="CANCELLED">Cancel</option>
                      </select>
                    )}
                    {(user.role === "PATIENT" || user.role === "ADMIN" || user.role === "RECEPTIONIST") &&
                      a.status !== "CANCELLED" && a.status !== "COMPLETED" && (
                        <button className="danger" onClick={() => handleCancel(a.id)}>Cancel</button>
                      )}
                  </td>
                </tr>
              ))}
              {appointments.length === 0 && <tr><td colSpan={6}>No appointments found</td></tr>}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
