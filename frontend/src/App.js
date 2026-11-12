import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Navbar from "./components/Navbar";

import Login from "./pages/auth/Login";
import Register from "./pages/auth/Register";

import AdminDashboard from "./pages/admin/AdminDashboard";
import DoctorDashboard from "./pages/doctor/DoctorDashboard";
import PatientDashboard from "./pages/patient/PatientDashboard";
import ReceptionistDashboard from "./pages/receptionist/ReceptionistDashboard";

import Patients from "./pages/Patients";
import PatientDetail from "./pages/PatientDetail";
import Doctors from "./pages/Doctors";
import Appointments from "./pages/Appointments";
import MedicalRecords from "./pages/MedicalRecords";
import Prescriptions from "./pages/Prescriptions";
import Billing from "./pages/Billing";
import Profile from "./pages/shared/Profile";
import Unauthorized from "./pages/shared/Unauthorized";

import "./styles/main.css";

const ROLE_HOME = {
  ADMIN: "/admin/dashboard",
  DOCTOR: "/doctor/dashboard",
  PATIENT: "/patient/dashboard",
  RECEPTIONIST: "/receptionist/dashboard",
};

function HomeRedirect() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={ROLE_HOME[user.role] || "/login"} replace />;
}

function AppShell({ children }) {
  return (
    <>
      <Navbar />
      {children}
    </>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/unauthorized" element={<Unauthorized />} />

          <Route path="/" element={<HomeRedirect />} />

          <Route path="/admin/dashboard" element={
            <ProtectedRoute allowedRoles={["ADMIN"]}><AppShell><AdminDashboard /></AppShell></ProtectedRoute>
          } />
          <Route path="/doctor/dashboard" element={
            <ProtectedRoute allowedRoles={["DOCTOR"]}><AppShell><DoctorDashboard /></AppShell></ProtectedRoute>
          } />
          <Route path="/patient/dashboard" element={
            <ProtectedRoute allowedRoles={["PATIENT"]}><AppShell><PatientDashboard /></AppShell></ProtectedRoute>
          } />
          <Route path="/receptionist/dashboard" element={
            <ProtectedRoute allowedRoles={["RECEPTIONIST"]}><AppShell><ReceptionistDashboard /></AppShell></ProtectedRoute>
          } />

          <Route path="/patients" element={
            <ProtectedRoute allowedRoles={["ADMIN", "DOCTOR", "RECEPTIONIST"]}><AppShell><Patients /></AppShell></ProtectedRoute>
          } />
          <Route path="/patients/:id" element={
            <ProtectedRoute allowedRoles={["ADMIN", "DOCTOR", "RECEPTIONIST"]}><AppShell><PatientDetail /></AppShell></ProtectedRoute>
          } />
          <Route path="/doctors" element={
            <ProtectedRoute allowedRoles={["ADMIN", "DOCTOR", "PATIENT", "RECEPTIONIST"]}><AppShell><Doctors /></AppShell></ProtectedRoute>
          } />
          <Route path="/appointments" element={
            <ProtectedRoute allowedRoles={["ADMIN", "DOCTOR", "PATIENT", "RECEPTIONIST"]}><AppShell><Appointments /></AppShell></ProtectedRoute>
          } />
          <Route path="/medical-records" element={
            <ProtectedRoute allowedRoles={["ADMIN", "DOCTOR", "PATIENT"]}><AppShell><MedicalRecords /></AppShell></ProtectedRoute>
          } />
          <Route path="/prescriptions" element={
            <ProtectedRoute allowedRoles={["ADMIN", "DOCTOR", "PATIENT"]}><AppShell><Prescriptions /></AppShell></ProtectedRoute>
          } />
          <Route path="/billing" element={
            <ProtectedRoute allowedRoles={["ADMIN", "RECEPTIONIST", "PATIENT"]}><AppShell><Billing /></AppShell></ProtectedRoute>
          } />
          <Route path="/profile" element={
            <ProtectedRoute><AppShell><Profile /></AppShell></ProtectedRoute>
          } />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
