import { apiClient } from '@/services/axiosService';

interface GroupPayload {
  name: string;
  userId: string;
}

export interface GroupResponse {
  id: string;
  name: string;
}

export async function postGroup(payload: GroupPayload) {
  return await apiClient.post<GroupResponse>('group', payload);
}

export async function getGroups(userId: string) {
  return await apiClient.get<GroupResponse[]>(`group/${userId}`);
}
