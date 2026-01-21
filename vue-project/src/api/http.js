import axios from 'axios'

// Tiny axios wrapper configured with baseURL from env and sensible defaults.
// Usage:
// import api from '@/api/http'
// api.get('/users')

const baseURL = import.meta.env.VITE_API_BASE || '/api'

const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
})

// Optional: add a response interceptor to unwrap data or handle auth
api.interceptors.response.use(
  (res) => res,
  (err) => {
    // You can extend this to handle 401 (redirect to login), show toast, etc.
    return Promise.reject(err)
  }
)

export default api
