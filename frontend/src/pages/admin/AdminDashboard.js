import React, { useEffect, useState } from "react";
import dashboardService from "../../services/dashboardService";
import Loading from "../../components/Loading";
import ErrorMessage from "../../components/ErrorMessage";
import { extractErrorMessage } from "../../utils/errorUtils";
import StatusBadge from "../shared/StatusBadge";

export default function AdminDashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    dashboardService.getAdmin()
      .then(setData)
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loading />;

  return (
    <div className="page-container">
      <h2>Admin Dashboard</h2>
      <ErrorMessage message={error} />
      {data && (
        <>
          <div className="stats-grid">
            <div className="stat-card"><div className="stat-value">{data.totalPatients}</div><div className="stat-label">Total Patients</div></div>
            <div className="stat-card"><div className="stat-value">{data.totalDoctors}</div><div className="stat-label">Total Doctors</div></div>
            <div className="stat-card"><div className="stat-value">{data.totalAppointments}</div><div className="stat-label">Total Appointments</div></div>
            <div className="stat-card"><div className="stat-value">${Number(data.totalRevenue).toLocaleString()}</div><div className="stat-label">Revenue</div></div>
          </div>

          <div className="card" style={{ marginTop: "1.5rem" }}>
            <h3>Recent Appointments</h3>
            <table>
              <thead><tr><th>Patient</th><th>Doctor</th><th>Date/Time</th><th>Status</th></tr></thead>
              <tbody>
                {data.recentAppointments.map((a) => (
                  <tr key={a.id}>
                    <td>{a.patientName}</td>
                    <td>{a.doctorName}</td>
                    <td>{new Date(a.appointmentDateTime).toLocaleString()}</td>
                    <td><StatusBadge status={a.status} /></td>
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
