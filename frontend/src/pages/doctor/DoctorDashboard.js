import React, { useEffect, useState } from "react";
import dashboardService from "../../services/dashboardService";
import Loading from "../../components/Loading";
import ErrorMessage from "../../components/ErrorMessage";
import { extractErrorMessage } from "../../utils/errorUtils";
import StatusBadge from "../shared/StatusBadge";

export default function DoctorDashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    dashboardService.getDoctor()
      .then(setData)
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loading />;

  return (
    <div className="page-container">
      <h2>Doctor Dashboard</h2>
      <ErrorMessage message={error} />
      {data && (
        <>
          <div className="stats-grid">
            <div className="stat-card"><div className="stat-value">{data.todaysAppointments.length}</div><div className="stat-label">Today's Appointments</div></div>
            <div className="stat-card"><div className="stat-value">{data.upcomingAppointments.length}</div><div className="stat-label">Upcoming</div></div>
            <div className="stat-card"><div className="stat-value">{data.assignedPatientCount}</div><div className="stat-label">Assigned Patients</div></div>
          </div>

          <div className="card" style={{ marginTop: "1.5rem" }}>
            <h3>Today's Appointments</h3>
            <table>
              <thead><tr><th>Patient</th><th>Time</th><th>Reason</th><th>Status</th></tr></thead>
              <tbody>
                {data.todaysAppointments.map((a) => (
                  <tr key={a.id}>
                    <td>{a.patientName}</td>
                    <td>{new Date(a.appointmentDateTime).toLocaleTimeString()}</td>
                    <td>{a.reason}</td>
                    <td><StatusBadge status={a.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="card">
            <h3>Recent Medical Records</h3>
            <table>
              <thead><tr><th>Patient</th><th>Diagnosis</th><th>Date</th></tr></thead>
              <tbody>
                {data.recentMedicalRecords.map((r) => (
                  <tr key={r.id}>
                    <td>{r.patientName}</td>
                    <td>{r.diagnosis}</td>
                    <td>{new Date(r.createdAt).toLocaleDateString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}
