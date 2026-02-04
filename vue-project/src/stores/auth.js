import { defineStore } from 'pinia';
import http from '@/api/http'; // Importamos tu configuración de axios

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null,
    isAuthenticated: !!localStorage.getItem('token'),
  }),

  actions: {
    async login(credentials) {
      try {
        // Petición al endpoint que definimos en el AuthController del backend
        const response = await http.post('/api/auth/login', credentials);

        const { token, userId, email, roles } = response.data;

        // Guardar en el estado de Pinia
        this.token = token;
        this.user = { userId, email, roles };
        this.isAuthenticated = true;

        // Persistencia para que no se borre al recargar la página
        localStorage.setItem('token', token);

        return { success: true };
      } catch (error) {
        console.error('Error en el login:', error);
        return {
          success: false,
          message: error.response?.data?.message || 'Error de conexión'
        };
      }
    },

    logout() {
      this.token = null;
      this.user = null;
      this.isAuthenticated = false;
      localStorage.removeItem('token');
    }
  }
});
