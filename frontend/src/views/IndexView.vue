<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
</script>

<template>
  <v-app>
    <v-container class="d-flex align-center justify-center">
      <v-row>
        <v-col cols="12">
          <v-card>
            <v-card-title>
              <h1>Index</h1>
            </v-card-title>
            <v-card-text>
              <p>Welcome to the Index page</p>
            </v-card-text>
            <v-card-actions>
              <v-btn color="primary" @click="router.push({ name: 'Home' })">Login</v-btn>
              <v-btn color="primary" @click="router.push({ name: 'Registration' })">Register</v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </v-app>
</template>

<style scoped></style>
