<script setup lang="ts">
import { useRouter } from 'vue-router';
import { onMounted, ref } from 'vue';
import {getGroups, type GroupResponse, updatePlayerStatus} from '@/services/groupRestService';
import { getUserMessages } from '@/services/messageRestService';
import { useMessageStore } from '@/store/MessageStore';
import { useAuthStore } from '@/store/AuthStore';

const authStore = useAuthStore();
const messageStore = useMessageStore();
const router = useRouter();
const groupData = ref<GroupResponse[]>([]);

const fetchMessages = async () => {
  getUserMessages(authStore.getUserId())
    .then((response) => {
      messageStore.setMessages(response.data);
    })
    .catch((error) => {
      console.error(error);
    });
};

const leaveGroup = (groupId: string) => {
  updatePlayerStatus(
    groupId,
    authStore.getUserId(),
    "LEAVED"
  )
    .then(() => {
      getGroups(authStore.getUserId())
        .then((response) => {
          groupData.value = response.data;
        })
        .catch((error) => {
          console.error(error);
        });
    })
    .catch((error) => {
      console.error(error);
    });
};

onMounted(() => {
  getGroups(authStore.getUserId())
    .then((response) => {
      groupData.value = response.data;
    })
    .catch((error) => {
      console.error(error);
    });
  fetchMessages();
});
</script>

<template>
  <v-app>
    <v-container class="d-flex align-center justify-center">
      <v-sheet>
        <v-card class="mx-auto pa-6" elevation="12" width="700" rounded>
          <v-card-title>
            <span class="headline">Groups</span>
          </v-card-title>
          <v-card-text>
            <v-table>
              <thead>
                <tr>
                  <th class="text-left">Name</th>
                  <th class="text-left">Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in groupData" :key="item.id">
                  <td>{{ item.name }}</td>
                  <td>
                    <v-btn color="primary" @click="router.push('group/' + item.id)">Details</v-btn>
                    <v-btn color="warning" @click="leaveGroup(item.id)">Verlassen</v-btn>
                  </td>
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

    <h3>UserName: {{ authStore.getUserName() }}</h3>
    <h3>Authenticated: {{ authStore.getAuthenticated() }}</h3>
    <h3>UserID: {{ authStore.getUserId() }}</h3>
    <h3>clientId: {{ authStore.getClientId() }}</h3>

    <h3>Email: {{ authStore.getEmail() }}</h3>

    <h3>Roles: {{ authStore.getRoles() }}</h3>

    <h3>Token: {{ authStore.getToken() }}</h3>

    <h3>Refresher Token: {{ authStore.getRefreshToken() }}</h3>
  </v-app>
</template>

<style scoped></style>
