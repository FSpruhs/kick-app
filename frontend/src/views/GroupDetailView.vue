<script setup lang="ts">
import { useRouter } from 'vue-router';
import { onMounted, ref } from 'vue';
import {
  getGroupDetails,
  type GroupDetailResponse,
  removePlayer
} from '@/services/groupRestService';
import { useGroupStore } from '@/store/GroupStore';
import {useAuthStore} from "@/store/AuthStore";

const router = useRouter();
const groupId = String(router.currentRoute.value.params.id);
const groupDetail = ref<GroupDetailResponse | null>(null);
const groupStore = useGroupStore();
const authStore = useAuthStore();

const handleRemovePlayer = (id: string) => {
  removePlayer({
    groupId: groupId,
    removeUserId: id,
    removingUserId: authStore.getUserId()
  })
    .then((response) => {
      console.log(response);
    })
    .catch((error) => {
      console.error(error);
    });
};

onMounted(() => {
  getGroupDetails(groupId)
    .then((response) => {
      groupDetail.value = response.data;
      const players = response.data.players.map((player) => {
        return {
          id: player.id,
          name: player.nickName,
          role: player.role,
          status: player.status
        };
      });
      const group = {
        id: response.data.id,
        name: response.data.name,
        players: players,
      };
      groupStore.saveGroup(group);
    })
    .catch((error) => {
      console.error(error);
    });
});
</script>

<template>
  <v-app>
    <v-container class="d-flex align-center justify-center">
      <v-sheet>
        <v-card class="mx-auto pa-6" elevation="12" width="1000" rounded>
          <v-card-title class="text-center">
            <span class="headline">Gruppe: {{ groupDetail?.name }}</span>
          </v-card-title>
          <v-card-text>
            <v-row>
              <v-col>
                <h3>Gruppenmitglieder</h3>
              </v-col>
            </v-row>
            <v-table>
              <thead>
                <tr>
                  <th class="text-left">ID</th>
                  <th class="text-left">Name</th>
                  <th class="text-left">Rolle</th>
                  <th class="text-left">Status</th>
                  <th class="text-left">Aktionen</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in groupDetail?.players" :key="item.id">
                  <td>{{ item.id }}</td>
                  <td>{{ item.nickName }}</td>
                  <td>{{ item.role }}</td>
                  <td>{{ item.status }}</td>
                  <td>
                    <v-btn
                      color="primary"
                      @click="router.push({ name: 'EditPlayer', params: { id: item.id } })"
                      >Bearbeiten</v-btn
                    >
                    <v-btn color="warning" @click="handleRemovePlayer(item.id)">Entfernen</v-btn>
                  </td>
                </tr>
              </tbody>
            </v-table>
          </v-card-text>
          <v-card-actions>
            <v-btn color="primary" @click="router.push({ name: 'GroupInvite' })"
              >Spieler einladen</v-btn
            >
            <v-btn color="primary" @click="router.push({ name: 'NewMatch' })"
              >Match erstellen</v-btn
            >
          </v-card-actions>
        </v-card>
      </v-sheet>
    </v-container>
  </v-app>
</template>

<style scoped></style>
