import axios, { type AxiosInstance } from 'axios';
import { getToken } from '@josempgon/vue-keycloak';

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

apiClient.interceptors.request.use(
  async (config) => {
    const token = await getToken();
    config.headers['Authorization'] = `Bearer ${token}`;
    return config;
  },
  (error) => {
    Promise.reject(error);
  }
);

export default apiClient;
