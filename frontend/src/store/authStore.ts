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
    updateToken(token: string, refreshToken: string) {
      this.authData!.token = token;
      this.authData!.refreshToken = refreshToken;
    },
    logout() {
      keycloak.logout({ redirectUri: 'http://localhost:5173/' });
      if (this.authData) {
        this.authData = null;
      }
    },
    getAuthenticatedData(): AuthData | null {
      return this.authData;
    }
  },
  persist: true
});
