import axios from 'axios'

const ApiAxios = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: parseInt(import.meta.env.VITE_API_TIMEOUT) || 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

ApiAxios.interceptors.request.use(
  (config) => {
    console.log(`🚀 API Request: ${config.method?.toUpperCase()} ${config.url}`)
    return config
  },
  (error) => {
    console.error('❌ Request Error:', error)
    return Promise.reject(error)
  }
)

ApiAxios.interceptors.response.use(
  (response) => {
    console.log(`✅ API Response: ${response.status} ${response.config.url}`)
    return response
  },
  (error) => {
    const { response, request, message } = error

    if (response) {
      console.error(`❌ Response Error: ${response.status} ${response.config.url}`, response.data)
    } else if (request) {
      console.error('❌ Network Error: No response received', request)
    } else {
      console.error('❌ Request Setup Error:', message)
    }

    return Promise.reject(error)
  }
)

export default ApiAxios
