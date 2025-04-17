export interface Message {
  id: string;
  text: string;
  type: string;
  occurredAt: string;
  isRead: boolean;
  userId: string;
  variables: Map<string, string>
}
