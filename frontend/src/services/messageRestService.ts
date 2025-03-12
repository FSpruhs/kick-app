import apiClient from '@/services/axiosService';

export interface Message {
  id: string;
  content: string;
  userId: string;
  type: string;
  occurredAt: string;
  read: boolean;
}

export async function getUserMessages(userId: string) {
  return await apiClient.get<Message[]>(`api/v1/message/user/${userId}`);
}

export async function readMessage(messageId: string) {
  return await apiClient.put(`api/v1/message/${messageId}/read`);
}
