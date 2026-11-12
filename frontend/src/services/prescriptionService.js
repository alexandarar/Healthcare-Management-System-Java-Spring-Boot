import api from "./api";

const prescriptionService = {
  create: (payload) => api.post("/prescriptions", payload).then((r) => r.data.data),
  getByPatient: (patientId) => api.get(`/prescriptions/${patientId}`).then((r) => r.data.data),
  getByDoctor: (doctorId) => api.get(`/prescriptions/doctor/${doctorId}`).then((r) => r.data.data),
};

export default prescriptionService;
