import apiClient from './api.service';

const AuthService = {
  async login(email, password) {
    // Log request details (without password) in development
    if (import.meta.env.DEV) {
      console.log('[AuthService] Login attempt', {
        email,
        endpoint: '/api/v1/auth/login',
        baseURL: apiClient.defaults.baseURL
      });
    }

    try {
      const response = await apiClient.post('/api/v1/auth/login', {
        email,
        password,
      });
      
      if (import.meta.env.DEV) {
        console.log('[AuthService] Login successful', {
          email,
          status: response.status,
          hasToken: !!response.data?.token
        });
      }
      
      const { token, username, email: userEmail, role } = response.data;
      
      if (token) {
        localStorage.setItem('auth_token', token);
        localStorage.setItem('auth_user', JSON.stringify({
          username,
          email: userEmail,
          role
        }));
      }
      
      return response.data;
    } catch (error) {
      // Log error details in development
      if (import.meta.env.DEV) {
        console.error('[AuthService] Login failed', {
          email,
          status: error.response?.status,
          statusText: error.response?.statusText,
          errorData: error.response?.data,
          message: error.message
        });
      }

      // Enhanced error handling
      if (error.response) {
        // Server responded with error
        const errorData = error.response.data;
        const status = error.response.status;
        
        if (status === 400 && errorData.fields) {
          // Validation errors
          const fieldMessages = Object.entries(errorData.fields)
            .map(([field, message]) => `${field}: ${message}`)
            .join(', ');
          throw new Error(`Validation error: ${fieldMessages}`);
        } else if (status === 401) {
          // Authentication failed
          throw new Error(errorData.message || 'Invalid email or password');
        } else if (errorData.message) {
          // Other server errors with message
          throw new Error(errorData.message);
        } else {
          throw new Error('Login failed. Please check your credentials.');
        }
      } else if (error.request) {
        // Request made but no response
        throw new Error('Cannot connect to server. Please check if the backend is running.');
      } else {
        // Something else happened
        throw new Error(error.message || 'An unexpected error occurred');
      }
    }
  },

  logout() {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
  },

  getCurrentUser() {
    const userStr = localStorage.getItem('auth_user');
    return userStr ? JSON.parse(userStr) : null;
  },

  getToken() {
    return localStorage.getItem('auth_token');
  },

  isAuthenticated() {
    return !!this.getToken();
  },
};

export default AuthService;
