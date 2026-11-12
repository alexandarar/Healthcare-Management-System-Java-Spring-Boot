import api from "./api";

const doctorService = {
  create: (payload) => api.post("/doctors", payload).then((r) => r.data.data),
  getAll: (specialization) => api.get("/doctors", { params: { specialization } }).then((r) => r.data.data),
  getMyProfile: () => api.get("/doctors/me").then((r) => r.data.data),
  getById: (id) => api.get(`/doctors/${id}`).then((r) => r.data.data),
  update: (id, payload) => api.put(`/doctors/${id}`, payload).then((r) => r.data.data),
  deactivate: (id) => api.delete(`/doctors/${id}`).then((r) => r.data),
};

export default doctorService;
