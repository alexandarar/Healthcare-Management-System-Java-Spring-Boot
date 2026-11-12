import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import ErrorMessage from "../../components/ErrorMessage";
import { extractErrorMessage } from "../../utils/errorUtils";

/**
 * Public self-registration always creates a PATIENT account. DOCTOR/ADMIN/RECEPTIONIST
 * accounts aren't created here — matches the backend, which rejects role=DOCTOR on
 * /api/auth/register and expects an admin to create doctor accounts explicitly.
 */
export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({
    fullName: "", email: "", password: "", phoneNumber: "",
    dateOfBirth: "", gender: "MALE", address: "", bloodGroup: "",
    emergencyContactName: "", emergencyContactPhone: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await register({ ...form, role: "PATIENT" });
      navigate("/patient/dashboard");
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-box" style={{ width: 460 }}>
        <h2>Create Patient Account</h2>
        <ErrorMessage message={error} />
        <form onSubmit={handleSubmit}>
          <div><label>Full Name</label><input required value={form.fullName} onChange={handleChange("fullName")} /></div>
          <div><label>Email</label><input type="email" required value={form.email} onChange={handleChange("email")} /></div>
          <div><label>Password</label><input type="password" required minLength={8} value={form.password} onChange={handleChange("password")} /></div>
          <div><label>Phone Number</label><input required value={form.phoneNumber} onChange={handleChange("phoneNumber")} /></div>
          <div><label>Date of Birth</label><input type="date" required value={form.dateOfBirth} onChange={handleChange("dateOfBirth")} /></div>
          <div>
            <label>Gender</label>
            <select value={form.gender} onChange={handleChange("gender")}>
              <option value="MALE">Male</option>
              <option value="FEMALE">Female</option>
              <option value="OTHER">Other</option>
            </select>
          </div>
          <div><label>Address</label><input required value={form.address} onChange={handleChange("address")} /></div>
          <div><label>Blood Group</label><input value={form.bloodGroup} onChange={handleChange("bloodGroup")} /></div>
          <div><label>Emergency Contact Name</label><input required value={form.emergencyContactName} onChange={handleChange("emergencyContactName")} /></div>
          <div><label>Emergency Contact Phone</label><input required value={form.emergencyContactPhone} onChange={handleChange("emergencyContactPhone")} /></div>
          <button className="primary" type="submit" disabled={loading}>
            {loading ? "Creating account..." : "Register"}
          </button>
        </form>
        <div className="switch-link">
          Already have an account? <Link to="/login">Sign in</Link>
        </div>
      </div>
    </div>
  );
}
