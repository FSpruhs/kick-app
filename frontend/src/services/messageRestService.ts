import apiClient from '@/services/axiosService';

export interface MessageResponse {
  id: string;
  text: string;
  userId: string;
  type: string;
  occurredAt: string;
  isRead: boolean;
  variables: Map<string, string>
}

export async function getUserMessages(userId: string) {
  return await apiClient.get<MessageResponse[]>(`api/v1/message/user/${userId}`);
}

export async function readMessage(messageId: string) {
  return await apiClient.put(`api/v1/message/${messageId}/read`);
}
