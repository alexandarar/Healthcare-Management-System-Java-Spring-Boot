import React, { useEffect, useState } from "react";
import dashboardService from "../../services/dashboardService";
import Loading from "../../components/Loading";
import ErrorMessage from "../../components/ErrorMessage";
import { extractErrorMessage } from "../../utils/errorUtils";
import StatusBadge from "../shared/StatusBadge";

export default function ReceptionistDashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    dashboardService.getReceptionist()
      .then(setData)
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loading />;

  return (
    <div className="page-container">
      <h2>Receptionist Dashboard</h2>
      <ErrorMessage message={error} />
      {data && (
        <>
          <div className="stats-grid">
            <div className="stat-card"><div className="stat-value">{data.totalPatientsRegisteredToday}</div><div className="stat-label">Registered Today</div></div>
            <div className="stat-card"><div className="stat-value">{data.todaysAppointments.length}</div><div className="stat-label">Today's Appointments</div></div>
            <div className="stat-card"><div className="stat-value">{data.pendingBillsCount}</div><div className="stat-label">Pending Bills</div></div>
          </div>

          <div className="card" style={{ marginTop: "1.5rem" }}>
            <h3>Today's Appointments</h3>
            <table>
              <thead><tr><th>Patient</th><th>Doctor</th><th>Time</th><th>Status</th></tr></thead>
              <tbody>
                {data.todaysAppointments.map((a) => (
                  <tr key={a.id}>
                    <td>{a.patientName}</td>
                    <td>{a.doctorName}</td>
                    <td>{new Date(a.appointmentDateTime).toLocaleTimeString()}</td>
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
