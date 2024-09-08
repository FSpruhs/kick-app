import { apiClient } from '@/services/axiosService';

interface GroupPayload {
  name: string;
  userId: string;
}

interface GroupResponse {
  id: string;
  name: string;
}

export async function postGroup(payload: GroupPayload) {
  return await apiClient.post<GroupResponse>('group', payload);
}
