import React from "react";

export default function Loading({ label = "Loading..." }) {
  return <div className="loading-spinner">{label}</div>;
}
