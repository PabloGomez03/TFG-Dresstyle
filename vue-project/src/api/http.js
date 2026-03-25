import axios from 'axios'

const baseURL = "/api"
const publicAuthPaths = ['/auth/login', '/auth/register']

const http = axios.create({
  baseURL: baseURL,
  headers: {
    'Content-Type': 'application/json',
  }
})

// Interceptor para agregar token JWT y CSRF
http.interceptors.request.use((config) => {
  const requestUrl = config.url || ''
  const token = localStorage.getItem('token')
  const isPublicAuthRequest = publicAuthPaths.some((path) => requestUrl.startsWith(path))

  if (!isPublicAuthRequest && token && token !== 'undefined') {
    config.headers.Authorization = `Bearer ${token}`
}


  const csrfToken = getCookie('XSRF-TOKEN')
  if (csrfToken) {
    config.headers['X-XSRF-TOKEN'] = csrfToken
  }

  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status

    if (status === 401 || status === 403) {
      localStorage.removeItem('token')
    }

    return Promise.reject(error)
  }
)

// Función auxiliar para leer cookies
function getCookie(name) {
  const value = `; ${document.cookie}`
  const parts = value.split(`; ${name}=`)
  if (parts.length === 2) return parts.pop().split(';').shift()
  return null
}

export default http
