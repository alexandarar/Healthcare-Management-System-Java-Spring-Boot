import React, { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import patientService from "../services/patientService";
import Loading from "../components/Loading";
import ErrorMessage from "../components/ErrorMessage";
import { extractErrorMessage } from "../utils/errorUtils";
import { Link } from "react-router-dom";

/** List + search of patients. Visible to ADMIN, DOCTOR, RECEPTIONIST per backend rules. */
export default function Patients() {
  const { user } = useAuth();
  const [patients, setPatients] = useState([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = (term) => {
    setLoading(true);
    patientService.getAll(term)
      .then(setPatients)
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    load(search);
  };

  const handleDeactivate = async (id) => {
    if (!window.confirm("Deactivate this patient?")) return;
    try {
      await patientService.deactivate(id);
      load(search);
    } catch (e) {
      setError(extractErrorMessage(e));
    }
  };

  return (
    <div className="page-container">
      <div className="section-header"><h2>Patients</h2></div>
      <ErrorMessage message={error} />

      <form className="search-bar" onSubmit={handleSearch}>
        <input placeholder="Search by name, email, phone..." value={search} onChange={(e) => setSearch(e.target.value)} />
        <button className="primary" type="submit">Search</button>
      </form>

      {loading ? <Loading /> : (
        <div className="card">
          <table>
            <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Blood Group</th><th>Status</th><th>Actions</th></tr></thead>
            <tbody>
              {patients.map((p) => (
                <tr key={p.id}>
                  <td>{p.fullName}</td>
                  <td>{p.email}</td>
                  <td>{p.phoneNumber}</td>
                  <td>{p.bloodGroup || "—"}</td>
                  <td>{p.active ? "Active" : "Inactive"}</td>
                  <td>
                    <Link to={`/patients/${p.id}`}>View</Link>
                    {user.role === "ADMIN" && p.active && (
                      <>{" | "}<button className="danger" onClick={() => handleDeactivate(p.id)}>Deactivate</button></>
                    )}
                  </td>
                </tr>
              ))}
              {patients.length === 0 && <tr><td colSpan={6}>No patients found</td></tr>}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
