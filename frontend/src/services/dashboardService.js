import api from "./api";

const dashboardService = {
  getAdmin: () => api.get("/dashboard/admin").then((r) => r.data.data),
  getDoctor: () => api.get("/dashboard/doctor").then((r) => r.data.data),
  getPatient: () => api.get("/dashboard/patient").then((r) => r.data.data),
  getReceptionist: () => api.get("/dashboard/receptionist").then((r) => r.data.data),
};

export default dashboardService;
