import axios, { type AxiosInstance } from 'axios';
import { useAuthStore } from '@/store/authStore';
import keycloak from '@/services/keycloakService';

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

const authStore = useAuthStore();

apiClient.interceptors.request.use(
  (config) => {
    // If user is authenticated, place access token in request header.
    if (authStore.getAuthenticatedData()?.authenticated) {
      config.headers['Authorization'] = `Bearer ${authStore.getAuthenticatedData()?.token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (res) => {
    return res;
  },
  async (error) => {
    const oriConfig = error.config;

    if (error.response?.status === 401 && !oriConfig._retry) {
      oriConfig._retry = true;

      try {
        await keycloak.updateToken(30);
        authStore.refhreshToken({
          token: keycloak.token ?? '',
          refreshToken: keycloak.refreshToken ?? ''
        });

        oriConfig.headers['Authorization'] = `Bearer ${authStore.getAuthenticatedData()?.token}`;

        return axiosInstance(oriConfig);
      } catch (_error) {
        console.error('Refresh token failed');
        console.error(_error);

        return Promise.reject(_error);
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
