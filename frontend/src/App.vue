<script setup lang="ts">
import { RouterView, useRouter } from 'vue-router';
import { useMessageStore } from '@/store/MessageStore';
import { useAuthStore } from '@/store/AuthStore';

const router = useRouter();
const authStore = useAuthStore();
const messageStore = useMessageStore();

function navigateToHome() {
  router.push({ name: 'Home' });
}

function logout() {
  authStore.logout();
  router.push({ name: 'Index' });
}
</script>

<template>
  <v-app>
    <v-app-bar :elevation="12" rounded>
      <template v-slot:prepend>
        <v-app-bar-nav-icon></v-app-bar-nav-icon>
      </template>
      <v-app-bar-title @click="navigateToHome" style="cursor: pointer">Kick App</v-app-bar-title>
      <v-spacer></v-spacer>
      <p @click="router.push({ name: 'Profile' })" style="cursor: pointer">
        {{ authStore.getUserName() }}
      </p>
      <v-btn @click="router.push({ name: 'Mailbox' })"
        ><v-icon>mdi-bell-outline</v-icon>
        <span class="notification-counter">{{ messageStore.getUnreadMessageCount() }}</span></v-btn
      >
      <v-btn @click="logout()"><v-icon>mdi-logout</v-icon></v-btn>
    </v-app-bar>
    <v-main>
      <v-container>
        <RouterView />
      </v-container>
    </v-main>
  </v-app>
</template>

<style scoped>
.notification-counter {
  background-color: red;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 50%;
  position: absolute;
  top: -1px;
  right: -1px;
}
</style>
