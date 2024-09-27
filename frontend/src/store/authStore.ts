import { defineStore } from 'pinia';
import keycloak from '@/services/keycloakService';
import type { AuthData } from '@/model/authData';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    authData: null as AuthData | null
  }),
  actions: {
    setAuthData(authData: AuthData) {
      this.authData = authData;
    },
    logout() {
      keycloak.logout();
      this.authenticated = false;
      this.userInfo = null;
    },
    getAuthenticatedData() {
      return this.authData;
    }
  },
  persist: true
});
