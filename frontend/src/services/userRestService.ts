import apiClient from '@/services/axiosService';

interface RegisterPayload {
  email: string;
  nickName: string;
}

interface RegisterResponse {
  id: string;
  email: string;
  nickName: string;
}

export interface UserInfo {
  id: string;
  email: string;
  nickName: string;
}

export async function postRegister(payload: RegisterPayload) {
  return await apiClient.post<RegisterResponse>('api/v1/user', payload);
}

export async function getUserAll(groupId: string) {
  return await apiClient.get<UserInfo[]>('api/v1/user?exceptGroupId=' + groupId);
}

export async function updateNickname(id: string, nickname: string) {
  return await apiClient.put(`api/v1/user/${id}/nickName?nickName=${nickname}`);
}
