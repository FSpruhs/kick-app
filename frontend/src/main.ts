import './assets/main.css';

import { createApp } from 'vue';
import { createPinia } from 'pinia';

import App from './App.vue';
import router from '@/router';

import 'vuetify/styles';
import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import * as directives from 'vuetify/directives';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';

import '@mdi/font/css/materialdesignicons.css';

import { aliases, mdi } from 'vuetify/iconsets/mdi';
import keycloak from '@/services/keycloakService';
import { useAuthStore } from '@/store/authStore';

const vuetify = createVuetify({
  components,
  directives,
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: {
      mdi
    }
  }
});

const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);

const app = createApp(App);

keycloak
  .init({ onLoad: 'login-required' })
  .then((authenticated) => {
    if (authenticated) {
      app.use(router);
      app.use(vuetify);
      app.use(pinia);
      app.mount('#app');

      const authStore = useAuthStore();

      authStore.setAuthData({
        token: keycloak.token ?? '',
        refreshToken: keycloak.refreshToken ?? '',
        userName: keycloak.tokenParsed?.preferred_username ?? '',
        userId: keycloak.tokenParsed?.sub ?? '',
        roles: keycloak.tokenParsed?.realm_access.roles ?? [],
        email: keycloak.tokenParsed?.email ?? '',
        authenticated: true
      });
    } else {
      window.location.reload();
    }
  })
  .catch(() => {
    console.error('Keycloak initialization failed:');
  });
