import axios, { type AxiosInstance } from 'axios';
import { apiClient } from '@/services/axiosService';

interface LoginUserPayload {
  email: string;
  password: string;
}

interface LoginUserResponse {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  nickName: string;
  groups: string[];
}

export default {
  async postLogin(payload: LoginUserPayload) {
    return await apiClient.post<LoginUserResponse>('user/login', payload);
  }
};
