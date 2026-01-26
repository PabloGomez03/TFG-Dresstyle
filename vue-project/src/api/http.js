import axios from 'axios'

// Tiny axios wrapper configured with baseURL from env and sensible defaults.
// Usage:
// import api from '@/api/http'
// api.get('/users')

const baseURL = import.meta.env.VITE_API_BASE || 'http://localhost:8080/ds'

const http = axios.create({
  baseURL: baseURL,
  headers: {
    'Content-Type': 'application/json'
  }
})

http.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default http;
