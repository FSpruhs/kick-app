<script setup lang="ts">
import { useUserStore } from '@/store/UserStore';
import { useRouter } from 'vue-router';

const userStore = useUserStore();
const router = useRouter();
</script>

<template>
  <v-app>
    <v-container class="d-flex align-center justify-center">
      <v-sheet>
        <v-card class="mx-auto pa-6" elevation="12" width="300" rounded>
          <v-card-title>
            <span class="headline">Groups</span>
          </v-card-title>
          <v-card-text>
            <v-table>
              <thead>
                <tr>
                  <th class="text-left">Name</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in userStore.getUser().groups" :key="item">
                  <td>{{ item }}</td>
                  <td>{{ item }}</td>
                </tr>
              </tbody>
            </v-table>
          </v-card-text>
          <v-card-actions>
            <v-btn color="primary" @click="router.push('group')">Neue Gruppe</v-btn>
          </v-card-actions>
        </v-card>
      </v-sheet>
    </v-container>
  </v-app>
</template>

<style scoped></style>
