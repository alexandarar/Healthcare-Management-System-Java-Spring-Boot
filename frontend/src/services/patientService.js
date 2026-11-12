import api from "./api";

const patientService = {
  getAll: (search) => api.get("/patients", { params: { search } }).then((r) => r.data.data),
  getMyProfile: () => api.get("/patients/me").then((r) => r.data.data),
  getById: (id) => api.get(`/patients/${id}`).then((r) => r.data.data),
  update: (id, payload) => api.put(`/patients/${id}`, payload).then((r) => r.data.data),
  deactivate: (id) => api.delete(`/patients/${id}`).then((r) => r.data),
};

export default patientService;
