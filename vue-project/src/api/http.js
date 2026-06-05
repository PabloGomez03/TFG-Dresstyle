import axios from 'axios'

const baseURL = "/api"
const publicAuthPaths = ['/auth/login', '/auth/register']
const publicApiPaths = ['/catalog/products', '/subscription/plans']

const http = axios.create({
  baseURL: baseURL,
  headers: {
    'Content-Type': 'application/json',
  }
})


http.interceptors.request.use((config) => {
  const requestUrl = config.url || ''
  let token = localStorage.getItem('token')
  const isPublicAuthRequest = publicAuthPaths.some((path) => requestUrl.startsWith(path))
  const isPublicApiRequest = publicApiPaths.some((path) => requestUrl.startsWith(path))

  
  if (token === 'undefined') {
    localStorage.removeItem('token')
    token = null
  }

  if (!isPublicAuthRequest && !isPublicApiRequest && token) {
    config.headers.Authorization = `Bearer ${token}`
  }


  const csrfToken = getCookie('XSRF-TOKEN')
  if (csrfToken) {
    config.headers['X-XSRF-TOKEN'] = csrfToken
  }

  return config
})


http.interceptors.request.use((config) => {
  try {
    if (config.url && config.url.includes('/orders/cart')) {
      const hasAuth = !!config.headers?.Authorization
      console.debug('[http] PUT /orders/cart auth:', hasAuth, 'url:', config.url)
    }
  } catch {  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status

    
    
    if (status === 401) {
      localStorage.removeItem('token')
    }

    return Promise.reject(error)
  }
)


function getCookie(name) {
  const value = `; ${document.cookie}`
  const parts = value.split(`; ${name}=`)
  if (parts.length === 2) return parts.pop().split(';').shift()
  return null
}

export default http
