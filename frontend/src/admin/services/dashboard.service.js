import apiClient from './api.service';

const DashboardService = {
  async getKPIs() {
    const response = await apiClient.get('/api/v1/dashboard/kpis');
    return response.data;
  },

  async getLeadsByStage() {
    const response = await apiClient.get('/api/v1/dashboard/leads-by-stage');
    return response.data;
  },

  async getRecentActivity() {
    const response = await apiClient.get('/api/v1/dashboard/recent-activity');
    return response.data;
  },
};

export default DashboardService;
