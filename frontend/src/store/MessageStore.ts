import { defineStore } from 'pinia';
import type { Message } from '@/model/message';
const LOCAL_STORAGE_NAME = 'message';

export const useMessageStore = defineStore('message', {
  state: () => ({
    messages: (JSON.parse(localStorage.getItem(LOCAL_STORAGE_NAME) ?? 'null') || []) as Message[]
  }),
  actions: {
    setMessages(messages: Message[]) {
      this.messages = messages;
      localStorage.setItem(LOCAL_STORAGE_NAME, JSON.stringify(this.messages));
    },
    getMessages() {
      return this.messages;
    },
    getUnreadMessageCount() {
      return this.messages.filter((message) => !message.read).length;
    }
  }
});
