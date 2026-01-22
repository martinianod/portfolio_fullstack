import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Log configuration in development
if (import.meta.env.DEV) {
  console.log('[API Client] Configuration', {
    baseURL: API_BASE_URL,
    env: {
      VITE_API_URL: import.meta.env.VITE_API_URL,
      mode: import.meta.env.MODE
    }
  });
}

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token and log requests
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // Log request in development
    if (import.meta.env.DEV) {
      console.log('[API Client] Request', {
        method: config.method?.toUpperCase(),
        url: config.url,
        fullURL: `${config.baseURL}${config.url}`,
        headers: {
          'Content-Type': config.headers['Content-Type'],
          'Authorization': token ? 'Bearer [REDACTED]' : 'none'
        },
        hasData: !!config.data
      });
    }
    
    return config;
  },
  (error) => {
    if (import.meta.env.DEV) {
      console.error('[API Client] Request error', error);
    }
    return Promise.reject(error);
  }
);

// Response interceptor to handle 401 errors and log responses
apiClient.interceptors.response.use(
  (response) => {
    if (import.meta.env.DEV) {
      console.log('[API Client] Response', {
        status: response.status,
        url: response.config.url,
        hasData: !!response.data
      });
    }
    return response;
  },
  (error) => {
    if (import.meta.env.DEV) {
      console.error('[API Client] Response error', {
        status: error.response?.status,
        url: error.config?.url,
        data: error.response?.data
      });
    }
    
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_user');
      window.location.href = '/admin/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
