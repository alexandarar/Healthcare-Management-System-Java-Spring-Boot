import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/**
 * Gate for any route that needs auth, optionally restricted to a set of roles.
 * Unauthenticated -> /login. Wrong role -> /unauthorized. This is a UX convenience only;
 * the backend enforces the real access control via @PreAuthorize on every endpoint.
 */
export default function ProtectedRoute({ children, allowedRoles }) {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
}
