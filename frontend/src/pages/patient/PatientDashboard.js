import React, { useEffect, useState } from "react";
import dashboardService from "../../services/dashboardService";
import Loading from "../../components/Loading";
import ErrorMessage from "../../components/ErrorMessage";
import { extractErrorMessage } from "../../utils/errorUtils";
import StatusBadge from "../shared/StatusBadge";

export default function PatientDashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    dashboardService.getPatient()
      .then(setData)
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Loading />;

  return (
    <div className="page-container">
      <h2>My Dashboard</h2>
      <ErrorMessage message={error} />
      {data && (
        <>
          <div className="card">
            <h3>Upcoming Appointments</h3>
            <table>
              <thead><tr><th>Doctor</th><th>Date/Time</th><th>Reason</th><th>Status</th></tr></thead>
              <tbody>
                {data.upcomingAppointments.map((a) => (
                  <tr key={a.id}>
                    <td>{a.doctorName} ({a.doctorSpecialization})</td>
                    <td>{new Date(a.appointmentDateTime).toLocaleString()}</td>
                    <td>{a.reason}</td>
                    <td><StatusBadge status={a.status} /></td>
                  </tr>
                ))}
                {data.upcomingAppointments.length === 0 && <tr><td colSpan={4}>No upcoming appointments</td></tr>}
              </tbody>
            </table>
          </div>

          <div className="card">
            <h3>Recent Prescriptions</h3>
            <table>
              <thead><tr><th>Doctor</th><th>Medicines</th><th>Date</th></tr></thead>
              <tbody>
                {data.prescriptions.slice(0, 5).map((p) => (
                  <tr key={p.id}>
                    <td>{p.doctorName}</td>
                    <td>{p.medicines.map((m) => m.name).join(", ")}</td>
                    <td>{new Date(p.createdAt).toLocaleDateString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="card">
            <h3>Bills</h3>
            <table>
              <thead><tr><th>Invoice</th><th>Total</th><th>Balance Due</th><th>Status</th></tr></thead>
              <tbody>
                {data.bills.slice(0, 5).map((b) => (
                  <tr key={b.id}>
                    <td>{b.invoiceNumber}</td>
                    <td>${Number(b.totalAmount).toFixed(2)}</td>
                    <td>${Number(b.balanceDue).toFixed(2)}</td>
                    <td><StatusBadge status={b.status} /></td>
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
