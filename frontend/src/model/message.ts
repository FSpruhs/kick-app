export interface Message {
  id: string;
  content: string;
  groupId?: string;
  type: string;
  occurredAt: string;
  read: boolean;
}
