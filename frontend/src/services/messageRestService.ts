import apiClient from '@/services/axiosService';

export interface Message {
  id: string;
  content: string;
  userId: string;
  type: string;
  occurredAt: string;
  read: boolean;
}

export interface MessageReadPayload {
  messageId: string;
  userId: string;
  read: boolean;
}

export async function getUserMessages(userId: string) {
  return await apiClient.get<Message[]>('api/v1/message/' + userId);
}

export async function readMessage(payload: MessageReadPayload) {
  return await apiClient.put('api/v1/message/read', payload);
}
