import React, { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import doctorService from "../services/doctorService";
import Loading from "../components/Loading";
import ErrorMessage from "../components/ErrorMessage";
import SuccessMessage from "../components/SuccessMessage";
import { extractErrorMessage } from "../utils/errorUtils";

const emptyForm = {
  fullName: "", email: "", password: "", phoneNumber: "",
  specialization: "", qualification: "", yearsOfExperience: "",
  consultationFee: "", availability: "",
};

export default function Doctors() {
  const { user } = useAuth();
  const [doctors, setDoctors] = useState([]);
  const [search, setSearch] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const load = (specialization) => {
    setLoading(true);
    doctorService.getAll(specialization)
      .then(setDoctors)
      .catch((e) => setError(extractErrorMessage(e)))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const handleSearch = (e) => { e.preventDefault(); load(search); };

  const handleCreate = async (e) => {
    e.preventDefault();
    setError(""); setSuccess("");
    try {
      await doctorService.create(form);
      setSuccess("Doctor added successfully");
      setForm(emptyForm);
      setShowForm(false);
      load(search);
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handleDeactivate = async (id) => {
    if (!window.confirm("Deactivate this doctor?")) return;
    try {
      await doctorService.deactivate(id);
      load(search);
    } catch (e) {
      setError(extractErrorMessage(e));
    }
  };

  return (
    <div className="page-container">
      <div className="section-header">
        <h2>Doctors</h2>
        {user.role === "ADMIN" && (
          <button className="primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? "Cancel" : "+ Add Doctor"}
          </button>
        )}
      </div>

      <ErrorMessage message={error} />
      <SuccessMessage message={success} />

      {showForm && (
        <div className="card">
          <h3>New Doctor</h3>
          <form onSubmit={handleCreate}>
            <div><label>Full Name</label><input required value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} /></div>
            <div><label>Email</label><input type="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></div>
            <div><label>Password</label><input type="password" required minLength={8} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} /></div>
            <div><label>Phone</label><input required value={form.phoneNumber} onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} /></div>
            <div><label>Specialization</label><input required value={form.specialization} onChange={(e) => setForm({ ...form, specialization: e.target.value })} /></div>
            <div><label>Qualification</label><input required value={form.qualification} onChange={(e) => setForm({ ...form, qualification: e.target.value })} /></div>
            <div><label>Years of Experience</label><input type="number" required value={form.yearsOfExperience} onChange={(e) => setForm({ ...form, yearsOfExperience: e.target.value })} /></div>
            <div><label>Consultation Fee</label><input type="number" step="0.01" value={form.consultationFee} onChange={(e) => setForm({ ...form, consultationFee: e.target.value })} /></div>
            <div><label>Availability</label><input placeholder="MON-FRI 09:00-17:00" value={form.availability} onChange={(e) => setForm({ ...form, availability: e.target.value })} /></div>
            <button className="primary" type="submit">Create Doctor</button>
          </form>
        </div>
      )}

      <form className="search-bar" onSubmit={handleSearch}>
        <input placeholder="Search by specialization..." value={search} onChange={(e) => setSearch(e.target.value)} />
        <button className="primary" type="submit">Search</button>
      </form>

      {loading ? <Loading /> : (
        <div className="card">
          <table>
            <thead><tr><th>Name</th><th>Specialization</th><th>Experience</th><th>Fee</th><th>Status</th>{user.role === "ADMIN" && <th>Actions</th>}</tr></thead>
            <tbody>
              {doctors.map((d) => (
                <tr key={d.id}>
                  <td>{d.fullName}</td>
                  <td>{d.specialization}</td>
                  <td>{d.yearsOfExperience} yrs</td>
                  <td>{d.consultationFee ? `$${d.consultationFee}` : "—"}</td>
                  <td>{d.active ? "Active" : "Inactive"}</td>
                  {user.role === "ADMIN" && (
                    <td>{d.active && <button className="danger" onClick={() => handleDeactivate(d.id)}>Deactivate</button>}</td>
                  )}
                </tr>
              ))}
              {doctors.length === 0 && <tr><td colSpan={6}>No doctors found</td></tr>}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
