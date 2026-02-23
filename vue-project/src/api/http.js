import axios from 'axios'

const baseURL = "/api"

const http = axios.create({
  baseURL: baseURL,
  headers: {
    'Content-Type': 'application/json',
  }
})

// Interceptor para agregar token JWT y CSRF
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')

  if (token && token !== 'undefined') {
    config.headers.Authorization = `Bearer ${token}`
}

  // Obtener token CSRF de las cookies
  const csrfToken = getCookie('XSRF-TOKEN')
  if (csrfToken) {
    config.headers['X-XSRF-TOKEN'] = csrfToken
  }

  return config
})

// Función auxiliar para leer cookies
function getCookie(name) {
  const value = `; ${document.cookie}`
  const parts = value.split(`; ${name}=`)
  if (parts.length === 2) return parts.pop().split(';').shift()
  return null
}

export default http
