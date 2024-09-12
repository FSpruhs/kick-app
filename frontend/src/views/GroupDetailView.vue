<script setup lang="ts">
import { useRouter } from 'vue-router';
import { onMounted, ref } from 'vue';
import { getGroupDetails, type GroupDetailResponse } from '@/services/groupRestService';

const router = useRouter();
const groupId = router.currentRoute.value.params.id;
const groupDetail = ref<GroupDetailResponse | null>(null);

onMounted(() => {
  getGroupDetails(groupId)
    .then((response) => {
      groupDetail.value = response.data;
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
        <v-card class="mx-auto pa-6" elevation="12" width="700" rounded>
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
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in groupDetail?.users" :key="item.id">
                  <td>{{ item.id }}</td>
                  <td>{{ item.name }}</td>
                </tr>
              </tbody>
            </v-table>
          </v-card-text>
          <v-card-actions>
            <v-btn color="primary" @click="router.push({ name: 'GroupInvite' })"
              >Spieler einladen</v-btn
            >
          </v-card-actions>
        </v-card>
      </v-sheet>
    </v-container>
  </v-app>
</template>

<style scoped></style>
