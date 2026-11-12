import api from "./api";

const appointmentService = {
  book: (payload) => api.post("/appointments", payload).then((r) => r.data.data),
  getAll: () => api.get("/appointments").then((r) => r.data.data),
  getById: (id) => api.get(`/appointments/${id}`).then((r) => r.data.data),
  getByPatient: (patientId) => api.get(`/appointments/patient/${patientId}`).then((r) => r.data.data),
  getByDoctor: (doctorId) => api.get(`/appointments/doctor/${doctorId}`).then((r) => r.data.data),
  reschedule: (id, payload) => api.put(`/appointments/${id}/reschedule`, payload).then((r) => r.data.data),
  updateStatus: (id, payload) => api.put(`/appointments/${id}/status`, payload).then((r) => r.data.data),
  cancel: (id) => api.delete(`/appointments/${id}`).then((r) => r.data),
};

export default appointmentService;
