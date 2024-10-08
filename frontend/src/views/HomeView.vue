<script setup lang="ts">
import { useUserStore } from '@/store/UserStore';
import { useRouter } from 'vue-router';
import { onMounted, ref } from 'vue';
import { getGroups, type GroupResponse, userLeaveGroup } from '@/services/groupRestService';
import { getUserMessages } from '@/services/messageRestService';
import { useMessageStore } from '@/store/MessageStore';
import { useAuthStore } from '@/store/authStore';
import keycloak from '../services/keycloakService';

const userStore = useUserStore();
const authStore = useAuthStore();
const messageStore = useMessageStore();
const router = useRouter();
const groupData = ref<GroupResponse>([]);

const fetchMessages = async () => {
  getUserMessages(userStore.getUser().id)
    .then((response) => {
      messageStore.setMessages(response.data);
    })
    .catch((error) => {
      console.error(error);
    });
};

const leaveGroup = (groupId) => {
  console.log('Leave Group: ' + groupId);
  userLeaveGroup({
    userId: userStore.getUser().id,
    groupId: groupId
  })
    .then(() => {
      getGroups(userStore.getUser().id)
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
  getGroups(userStore.getUser().id)
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

    <h3>UserName: {{ authStore.getAuthenticatedData().userName }}</h3>
    <h3>Authenticated: {{ authStore.getAuthenticatedData().authenticated }}</h3>
    <h3>UserID: {{ authStore.getAuthenticatedData().userId }}</h3>
    <h3>clientId: {{ keycloak.clientId }}</h3>

    <h3>Email: {{ authStore.getAuthenticatedData().email }}</h3>

    <h3>Roles: {{ authStore.getAuthenticatedData().roles }}</h3>

    <h3>Token: {{ authStore.getAuthenticatedData().token }}</h3>

    <h3>Refresher Token: {{ authStore.getAuthenticatedData().refreshToken }}</h3>
  </v-app>
</template>

<style scoped></style>
