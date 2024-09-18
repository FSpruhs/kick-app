<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useGroupStore } from '@/store/GroupStore';
import type { VForm } from 'vuetify/components';
import { ref } from 'vue';
import { updatePlayerRole } from '@/services/groupRestService';
import { useUserStore } from '@/store/UserStore';

const router = useRouter();
const groupStore = useGroupStore();
const userStore = useUserStore();
const playerId = router.currentRoute.value.params.id;
const player = groupStore.getPlayer(playerId);
const selectedRole = ref(player?.role || '');
const selectedStatus = ref(player?.status || '');

const submit = () => {
  if (player.status !== selectedStatus.value) {
    console.log(selectedStatus.value);
  }

  if (player.role !== selectedRole.value) {
    updatePlayerRole({
      groupId: groupStore.getGroup().id,
      updatedUserId: player.id,
      updatingUserId: userStore.getUser().id,
      newRole: selectedRole.value
    }).then(() => {
      console.log('Role updated');
    });
  }

  router.push({ name: 'GroupDetail', params: { id: groupStore.getGroup().id } });
};
</script>

<template>
  <v-form ref="editPlayerForm" @submit.prevent="submit">
    <v-text-field
      color="primary"
      v-model="player.name"
      label="Spieler Name"
      readonly
    ></v-text-field>
    <v-select
      v-model="selectedRole"
      :items="['admin', 'master', 'member']"
      label="Rolle"
    ></v-select>
    <v-select
      v-model="selectedStatus"
      :items="['active', 'inactive', 'removed', 'leaved', 'not found']"
      label="Status"
    ></v-select>
    <v-btn class="mt-2" type="submit" block>Speichern</v-btn>
  </v-form>
</template>

<style scoped></style>
