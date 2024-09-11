import { apiClient } from '@/services/axiosService';

interface GroupPayload {
  name: string;
  userId: string;
}

export interface GroupResponse {
  id: string;
  name: string;
}

export interface GroupDetailResponse {
  id: string;
  name: string;
  users: string[];
  inviteLevel: number;
}

export async function postGroup(payload: GroupPayload) {
  return await apiClient.post<GroupResponse>('api/v1/group', payload);
}

export async function getGroups(userId: string) {
  return await apiClient.get<GroupResponse[]>(`api/v1/group/user/${userId}`);
}

export async function getGroupDetails(groupId: string) {
  return await apiClient.get<GroupDetailResponse>(`api/v1/group/${groupId}`);
}
