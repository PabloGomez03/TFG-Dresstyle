import { defineStore } from 'pinia';
import http from '@/api/http'; // Importamos tu configuración de axios

function normalizeRoles(roles) {
  const roleList = Array.isArray(roles) ? roles : roles ? [roles] : [];

  return roleList
    .map((role) => String(role || '').trim().toUpperCase())
    .filter(Boolean);
}

function hasAdminRole(roles) {
  const normalizedRoles = normalizeRoles(roles);
  return normalizedRoles.includes('ROLE_ADMIN') || normalizedRoles.includes('ADMIN');
}

function decodeJwtPayload(token) {
  try {
    const payload = token.split('.')[1];
    if (!payload) return null;

    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const normalized = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=');
    const decoded = atob(normalized);

    return JSON.parse(decoded);
  } catch {
    return null;
  }
}

function isTokenExpired(token) {
  const payload = decodeJwtPayload(token);
  if (!payload?.exp) return true;

  const nowInSeconds = Math.floor(Date.now() / 1000);
  return payload.exp <= nowInSeconds;
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null,
    isAuthenticated: !!localStorage.getItem('token'),
  }),

  getters: {
    userRoles: (state) => normalizeRoles(state.user?.roles),
    isAdmin() {
      return this.isAuthenticated && hasAdminRole(this.userRoles);
    }
  },

  actions: {
    async login(credentials) {
      try {
        // Petición al endpoint que definimos en el AuthController del backend
        const response = await http.post('/auth/login', credentials);

        const { token, userId, email, roles } = response.data;
        const normalizedRoles = normalizeRoles(roles);

        // Guardar en el estado de Pinia
        this.token = token;
        this.user = { userId, email, roles: normalizedRoles };
        this.isAuthenticated = true;

        // Persistencia para que no se borre al recargar la página
        localStorage.setItem('token', token);

        return {
          success: true,
          roles: normalizedRoles,
          isAdmin: hasAdminRole(normalizedRoles)
        };
      } catch (error) {
        console.error('Error en el login:', error);
        return {
          success: false,
          status: error.response?.status || 500,
          message: error.response?.data?.message || 'Error de conexión'
        };
      }
    },

    initializeAuth() {
      const token = localStorage.getItem('token');

      if (!token || token === 'undefined' || isTokenExpired(token)) {
        this.logout();
        return;
      }

      const payload = decodeJwtPayload(token);
      const normalizedRoles = normalizeRoles(payload?.roles);
      this.token = token;
      this.user = {
        email: payload?.sub || null,
        roles: normalizedRoles
      };
      this.isAuthenticated = true;
    },

    isTokenValid() {
      if (!this.token || this.token === 'undefined') {
        return false;
      }

      return !isTokenExpired(this.token);
    },

    logout() {
      this.token = null;
      this.user = null;
      this.isAuthenticated = false;
      localStorage.removeItem('token');
    }
  }
});
