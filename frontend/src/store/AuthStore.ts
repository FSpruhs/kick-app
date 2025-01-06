import { defineStore } from 'pinia';
import { useKeycloak } from '@josempgon/vue-keycloak';

const keycloak = useKeycloak();

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: ''
  }),
  actions: {
    getToken() {
      return keycloak.keycloak.value?.token ?? '';
    },
    getUserName() {
      return keycloak.keycloak.value?.tokenParsed?.preferred_username;
    },
    getAuthenticated() {
      return keycloak.keycloak.value?.authenticated;
    },
    getUserId() {
      return keycloak.keycloak.value?.tokenParsed?.sub;
    },
    getClientId() {
      return keycloak.keycloak.value?.clientId;
    },
    getEmail() {
      return keycloak.keycloak.value?.tokenParsed?.email;
    },
    getRefreshToken() {
      return keycloak.keycloak.value?.refreshToken;
    },
    getRoles() {
      return keycloak.keycloak.value?.tokenParsed?.realm_access?.roles;
    },
    logout() {
      keycloak.keycloak.value?.logout({ redirectUri: window.location.origin });
    }
  }
});
