import api from "./api";

const medicalRecordService = {
  create: (payload) => api.post("/medical-records", payload).then((r) => r.data.data),
  update: (id, payload) => api.put(`/medical-records/${id}`, payload).then((r) => r.data.data),
  getByPatient: (patientId) => api.get(`/medical-records/${patientId}`).then((r) => r.data.data),
  getByDoctor: (doctorId) => api.get(`/medical-records/doctor/${doctorId}`).then((r) => r.data.data),
};

export default medicalRecordService;
