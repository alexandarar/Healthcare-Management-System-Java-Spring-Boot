import api from "./api";

const billingService = {
  generateBill: (payload) => api.post("/bills", payload).then((r) => r.data.data),
  recordPayment: (billId, payload) => api.post(`/bills/${billId}/payments`, payload).then((r) => r.data.data),
  getById: (id) => api.get(`/bills/detail/${id}`).then((r) => r.data.data),
  getByPatient: (patientId) => api.get(`/bills/${patientId}`).then((r) => r.data.data),
};

export default billingService;
