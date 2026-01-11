import axios from "axios";

// Create Axios instance with default config
const api = axios.create({
  baseURL: "/api/v1", // Relative path to be proxied
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000,
});

// Request Interceptor
api.interceptors.request.use(
  (config) => {
    // You can add auth tokens here in the future
    // const token = localStorage.getItem('token');
    // if (token) config.headers.Authorization = `Bearer ${token}`;
    console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor
api.interceptors.response.use(
  (response) => {
    console.log(`[API Response] ${response.status} ${response.config.url}`);
    return response.data; // Return direct data
  },
  (error) => {
    const message =
      error.response?.data?.message || "Something went wrong with the server";
    console.error("[API Error]", message);
    return Promise.reject({ ...error, message });
  }
);

export const ContactService = {
  /**
   * Send a new contact lead to the backend CRM
   * @param {Object} data - { name, email, message, source }
   */
  sendLead: async (data) => {
    // Simulate delay for better UX if local to avoid instant flash
    if (import.meta.env.DEV && !import.meta.env.VITE_USE_REAL_BACKEND) {
        console.log("Mocking backend request:", data);
        await new Promise(r => setTimeout(r, 1000));
        return { success: true, message: "Lead captured (Mock)" };
    }
    return api.post("/leads", data);
  },
};

export default api;
