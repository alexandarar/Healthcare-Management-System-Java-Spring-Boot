import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import patientService from "../services/patientService";
import medicalRecordService from "../services/medicalRecordService";
import appointmentService from "../services/appointmentService";
import Loading from "../components/Loading";
import ErrorMessage from "../components/ErrorMessage";
import { extractErrorMessage } from "../utils/errorUtils";

/** Full profile + medical history + appointment history for one patient (staff view). */
export default function PatientDetail() {
  const { id } = useParams();
  const [patient, setPatient] = useState(null);
  const [records, setRecords] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      patientService.getById(id),
      medicalRecordService.getByPatient(id).catch(() => []),
      appointmentService.getByPatient(id).catch(() => []),
    ])
      .then(([p, r, a]) => { setPatient(p); setRecords(r); setAppointments(a); })
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <Loading />;
  if (error) return <div className="page-container"><ErrorMessage message={error} /></div>;

  return (
    <div className="page-container">
      <h2>{patient.fullName}</h2>
      <div className="card">
        <p><strong>Email:</strong> {patient.email}</p>
        <p><strong>Phone:</strong> {patient.phoneNumber}</p>
        <p><strong>DOB:</strong> {patient.dateOfBirth}</p>
        <p><strong>Gender:</strong> {patient.gender}</p>
        <p><strong>Address:</strong> {patient.address}</p>
        <p><strong>Blood Group:</strong> {patient.bloodGroup || "—"}</p>
        <p><strong>Allergies:</strong> {patient.allergies || "None recorded"}</p>
        <p><strong>Emergency Contact:</strong> {patient.emergencyContactName} ({patient.emergencyContactPhone})</p>
      </div>

      <div className="card">
        <h3>Medical History</h3>
        <table>
          <thead><tr><th>Date</th><th>Doctor</th><th>Diagnosis</th><th>Symptoms</th></tr></thead>
          <tbody>
            {records.map((r) => (
              <tr key={r.id}>
                <td>{new Date(r.createdAt).toLocaleDateString()}</td>
                <td>{r.doctorName}</td>
                <td>{r.diagnosis}</td>
                <td>{r.symptoms}</td>
              </tr>
            ))}
            {records.length === 0 && <tr><td colSpan={4}>No records yet</td></tr>}
          </tbody>
        </table>
      </div>

      <div className="card">
        <h3>Appointment History</h3>
        <table>
          <thead><tr><th>Date</th><th>Doctor</th><th>Status</th></tr></thead>
          <tbody>
            {appointments.map((a) => (
              <tr key={a.id}>
                <td>{new Date(a.appointmentDateTime).toLocaleString()}</td>
                <td>{a.doctorName}</td>
                <td>{a.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
