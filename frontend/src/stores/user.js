import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))
  const role = ref(localStorage.getItem('role') || '')
  const token = ref(localStorage.getItem('token') || '')

  function setUser(userData, userRole) {
    user.value = userData
    role.value = userRole
    localStorage.setItem('user', JSON.stringify(userData))
    localStorage.setItem('role', userRole)
  }

  function setToken(tokenData) {
    token.value = tokenData
    localStorage.setItem('token', tokenData)
  }

  function logout() {
    user.value = {}
    role.value = ''
    token.value = ''
    localStorage.removeItem('user')
    localStorage.removeItem('role')
    localStorage.removeItem('token')
  }

  return { user, role, token, setUser, setToken, logout }
})
