import React from "react";
import { Link } from "react-router-dom";

export default function Unauthorized() {
  return (
    <div className="page-container">
      <div className="card">
        <h2>Access Denied</h2>
        <p>You don't have permission to view this page.</p>
        <Link to="/">Go back home</Link>
      </div>
    </div>
  );
}
