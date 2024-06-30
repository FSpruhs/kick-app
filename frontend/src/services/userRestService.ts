import axios, { type AxiosInstance } from 'axios'

interface LoginUserPayload {
  email: string
  password: string
}

interface LoginUserResponse {
  id: string
  firstName: string
  lastName: string
  email: string
  nickName: string
  groups: string[]
}

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

export default {
  async postLogin(payload: LoginUserPayload) {
    return await apiClient.post<LoginUserResponse>('user/login', payload)
  }
}
