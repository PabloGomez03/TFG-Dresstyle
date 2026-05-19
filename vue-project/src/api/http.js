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
  let token = localStorage.getItem('token')
  const isPublicAuthRequest = publicAuthPaths.some((path) => requestUrl.startsWith(path))

  // cleanup bogus 'undefined' token stored by mistakes
  if (token === 'undefined') {
    localStorage.removeItem('token')
    token = null
  }

  if (!isPublicAuthRequest && token) {
    config.headers.Authorization = `Bearer ${token}`
  }


  const csrfToken = getCookie('XSRF-TOKEN')
  if (csrfToken) {
    config.headers['X-XSRF-TOKEN'] = csrfToken
  }

  return config
})

// Debug helper: log when requests to orders cart are sent and whether Authorization present
http.interceptors.request.use((config) => {
  try {
    if (config.url && config.url.includes('/orders/cart')) {
      const hasAuth = !!config.headers?.Authorization
      console.debug('[http] PUT /orders/cart auth:', hasAuth, 'url:', config.url)
    }
  } catch { /* ignore */ }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status

    // only clear token on 401 (unauthenticated). A 403 is an authorization
    // failure and should not force logout automatically.
    if (status === 401) {
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
