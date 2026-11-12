import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const ROLE_HOME = {
  ADMIN: "/admin/dashboard",
  DOCTOR: "/doctor/dashboard",
  PATIENT: "/patient/dashboard",
  RECEPTIONIST: "/receptionist/dashboard",
};

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  if (!user) return null;

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to={ROLE_HOME[user.role] || "/"}>🏥 HMS</Link>
      </div>
      <div className="navbar-links">
        {user.role === "ADMIN" && (
          <>
            <Link to="/admin/dashboard">Dashboard</Link>
            <Link to="/patients">Patients</Link>
            <Link to="/doctors">Doctors</Link>
            <Link to="/appointments">Appointments</Link>
          </>
        )}
        {user.role === "DOCTOR" && (
          <>
            <Link to="/doctor/dashboard">Dashboard</Link>
            <Link to="/appointments">Appointments</Link>
            <Link to="/medical-records">Medical Records</Link>
            <Link to="/prescriptions">Prescriptions</Link>
          </>
        )}
        {user.role === "PATIENT" && (
          <>
            <Link to="/patient/dashboard">Dashboard</Link>
            <Link to="/appointments">Appointments</Link>
            <Link to="/medical-records">Medical Records</Link>
            <Link to="/prescriptions">Prescriptions</Link>
            <Link to="/billing">Bills</Link>
          </>
        )}
        {user.role === "RECEPTIONIST" && (
          <>
            <Link to="/receptionist/dashboard">Dashboard</Link>
            <Link to="/patients">Patients</Link>
            <Link to="/appointments">Appointments</Link>
            <Link to="/billing">Billing</Link>
          </>
        )}
        <Link to="/profile">Profile</Link>
      </div>
      <div className="navbar-user">
        <span>{user.fullName} ({user.role})</span>
        <button onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  );
}
