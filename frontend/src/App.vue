<script setup lang="ts">
import { RouterView } from 'vue-router';
</script>

<template>
  <v-app>
    <v-app-bar :elevation="12" rounded>
      <template v-slot:prepend>
        <v-app-bar-nav-icon></v-app-bar-nav-icon>
      </template>
      <v-app-bar-title>Kick App</v-app-bar-title>
      <v-spacer></v-spacer>
      <v-btn><v-icon>mdi-bell-outline</v-icon> <span class="notification-counter">5</span></v-btn>
      <v-btn><v-icon>mdi-logout</v-icon></v-btn>
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
