import './assets/main.css';

import { createApp } from 'vue';
import { createPinia } from 'pinia';

import App from './App.vue';

import 'vuetify/styles';
import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import * as directives from 'vuetify/directives';
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';

import '@mdi/font/css/materialdesignicons.css';

import { aliases, mdi } from 'vuetify/iconsets/mdi';
import { vueKeycloak } from '@josempgon/vue-keycloak';
import initRouter from '@/router';

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

await vueKeycloak.install(app, {
  config: {
    url: import.meta.env.VITE_KEYCLOAK_URL,
    realm: import.meta.env.VITE_KEYCLOAK_REALM,
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID
  },
  initOptions: {
    adapter: 'default',
    onLoad: 'check-sso'
  }
});

app.use(initRouter());
app.use(vuetify);
app.use(pinia);
app.mount('#app');
