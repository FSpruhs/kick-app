import apiClient from '@/services/axiosService';

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

interface RegisterPayload {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  nickName: string;
}

interface RegisterResponse {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  nickName: string;
  groups: string[];
}

export interface UserInfo {
  id: string;
  email: string;
  nickName: string;
}

export async function postLogin(payload: LoginUserPayload) {
  return await apiClient.post<LoginUserResponse>('api/v1/user/login', payload);
}

export async function postRegister(payload: RegisterPayload) {
  return await apiClient.post<RegisterResponse>('api/v1/user', payload);
}

export async function getUserAll(groupId: string) {
  return await apiClient.get<UserInfo[]>('api/v1/user?exceptGroupId=' + groupId);
}
