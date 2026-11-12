import React from "react";

export default function SuccessMessage({ message }) {
  if (!message) return null;
  return <div className="alert alert-success">{message}</div>;
}
