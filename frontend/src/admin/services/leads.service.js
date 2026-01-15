import apiClient from './api.service';

const LeadsService = {
  async getLeads(params = {}) {
    const response = await apiClient.get('/api/v1/leads', { params });
    return response.data;
  },

  async getLeadById(id) {
    const response = await apiClient.get(`/api/v1/leads/${id}`);
    return response.data;
  },

  async createLead(data) {
    const response = await apiClient.post('/api/v1/leads', data);
    return response.data;
  },

  async updateLead(id, data) {
    const response = await apiClient.put(`/api/v1/leads/${id}`, data);
    return response.data;
  },

  async deleteLead(id) {
    const response = await apiClient.delete(`/api/v1/leads/${id}`);
    return response.data;
  },

  async updateLeadStage(id, stage) {
    const response = await apiClient.patch(`/api/v1/leads/${id}/stage`, { stage });
    return response.data;
  },
};

export default LeadsService;
