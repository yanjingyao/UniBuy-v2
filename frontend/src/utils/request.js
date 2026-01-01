import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const service = axios.create({
  baseURL: 'http://localhost:8080', // Direct backend URL
  timeout: 5000
})

// Request interceptor
service.interceptors.request.use(
  config => {
    // Add token if exists (though API docs don't specify token header, 
    // usually it's Authorization or cookie. 
    // Since it's a simple school project, we might rely on sessions or just assume open for now 
    // or add user ID in headers if needed. 
    // But standard is token. I'll add it if user store has it.)
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = userStore.token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  response => {
    const res = response.data
    // Assuming backend returns { code: 200, msg: "...", data: ... }
    if (res.code !== 200) {
      // Suppress specific errors that are handled by UI logic
      const ignoredErrors = ['User not found', 'Merchant not found']
      if (res.msg && ignoredErrors.includes(res.msg)) {
          return Promise.reject(new Error(res.msg))
      }
      
      ElMessage.error(res.msg || 'Error')
      return Promise.reject(new Error(res.msg || 'Error'))
    } else {
      return res.data
    }
  },
  error => {
    console.log('err' + error)
    ElMessage.error(error.message)
    return Promise.reject(error)
  }
)

export default service
