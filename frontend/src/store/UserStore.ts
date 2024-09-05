import { defineStore } from 'pinia';
import type { User } from '@/model/user';
const LOCAL_STORAGE_NAME = 'user';

export const useUserStore = defineStore('user', {
  state: () => ({
    user: (JSON.parse(localStorage.getItem(LOCAL_STORAGE_NAME) ?? 'null') || null) as User
  }),
  actions: {
    saveUser(user: User) {
      console.log('Saving user', user);
      this.user = user;
      localStorage.setItem(LOCAL_STORAGE_NAME, JSON.stringify(this.user));
    },
    getUser() {
      return this.user;
    }
  }
});
