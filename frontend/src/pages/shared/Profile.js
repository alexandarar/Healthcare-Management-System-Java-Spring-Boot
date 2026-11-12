import React, { useEffect, useState } from "react";
import { useAuth } from "../../context/AuthContext";
import patientService from "../../services/patientService";
import doctorService from "../../services/doctorService";
import Loading from "../../components/Loading";
import ErrorMessage from "../../components/ErrorMessage";
import SuccessMessage from "../../components/SuccessMessage";
import { extractErrorMessage } from "../../utils/errorUtils";

export default function Profile() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const loader = user.role === "PATIENT" ? patientService.getMyProfile : doctorService.getMyProfile;
    if (user.role === "PATIENT" || user.role === "DOCTOR") {
      loader()
        .then((p) => { setProfile(p); setForm(p); })
        .catch((e) => setError(extractErrorMessage(e)))
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [user.role]);

  const handleSave = async (e) => {
    e.preventDefault();
    setError(""); setSuccess(""); setSaving(true);
    try {
      if (user.role === "PATIENT") {
        const updated = await patientService.update(profile.id, form);
        setProfile(updated);
      } else if (user.role === "DOCTOR") {
        const updated = await doctorService.update(profile.id, form);
        setProfile(updated);
      }
      setSuccess("Profile updated successfully");
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <Loading />;

  return (
    <div className="page-container">
      <h2>My Profile</h2>
      <ErrorMessage message={error} />
      <SuccessMessage message={success} />

      <div className="card">
        <p><strong>Name:</strong> {user.fullName}</p>
        <p><strong>Email:</strong> {user.email}</p>
        <p><strong>Role:</strong> {user.role}</p>
      </div>

      {profile && (user.role === "PATIENT" || user.role === "DOCTOR") && (
        <div className="card">
          <h3>Update Details</h3>
          <form onSubmit={handleSave}>
            <div><label>Full Name</label><input value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} /></div>
            <div><label>Phone</label><input value={form.phoneNumber} onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} /></div>

            {user.role === "PATIENT" && (
              <>
                <div><label>Address</label><input value={form.address || ""} onChange={(e) => setForm({ ...form, address: e.target.value })} /></div>
                <div><label>Blood Group</label><input value={form.bloodGroup || ""} onChange={(e) => setForm({ ...form, bloodGroup: e.target.value })} /></div>
                <div><label>Emergency Contact Name</label><input value={form.emergencyContactName || ""} onChange={(e) => setForm({ ...form, emergencyContactName: e.target.value })} /></div>
                <div><label>Emergency Contact Phone</label><input value={form.emergencyContactPhone || ""} onChange={(e) => setForm({ ...form, emergencyContactPhone: e.target.value })} /></div>
                <div><label>Allergies</label><input value={form.allergies || ""} onChange={(e) => setForm({ ...form, allergies: e.target.value })} /></div>
              </>
            )}

            {user.role === "DOCTOR" && (
              <>
                <div><label>Specialization</label><input value={form.specialization || ""} onChange={(e) => setForm({ ...form, specialization: e.target.value })} /></div>
                <div><label>Qualification</label><input value={form.qualification || ""} onChange={(e) => setForm({ ...form, qualification: e.target.value })} /></div>
                <div><label>Years of Experience</label><input type="number" value={form.yearsOfExperience || ""} onChange={(e) => setForm({ ...form, yearsOfExperience: e.target.value })} /></div>
                <div><label>Consultation Fee</label><input type="number" value={form.consultationFee || ""} onChange={(e) => setForm({ ...form, consultationFee: e.target.value })} /></div>
                <div><label>Availability</label><input value={form.availability || ""} onChange={(e) => setForm({ ...form, availability: e.target.value })} /></div>
              </>
            )}

            <button className="primary" type="submit" disabled={saving}>{saving ? "Saving..." : "Save Changes"}</button>
          </form>
        </div>
      )}
    </div>
  );
}
