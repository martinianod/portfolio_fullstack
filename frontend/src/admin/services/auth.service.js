import apiClient from './api.service';

const AuthService = {
  async login(email, password) {
    try {
      const response = await apiClient.post('/api/v1/auth/login', {
        email,
        password,
      });
      
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
      // Enhanced error handling
      if (error.response) {
        // Server responded with error
        const errorData = error.response.data;
        if (errorData.message) {
          throw new Error(errorData.message);
        } else if (errorData.fields) {
          // Validation errors
          const fieldMessages = Object.values(errorData.fields).join(', ');
          throw new Error(`Validation error: ${fieldMessages}`);
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
