<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useGroupStore } from '@/store/GroupStore';
import { ref } from 'vue';
import { updatePlayerRole } from '@/services/groupRestService';

const router = useRouter();
const groupStore = useGroupStore();
const playerId = router.currentRoute.value.params.id;
const player = groupStore.getPlayer(playerId as string);
const selectedRole = ref(player?.role || '');
const selectedStatus = ref(player?.status || '');

const submit = () => {
  if (!player) {
    return
  }
  if (player.role !== selectedRole.value || player.status !== selectedStatus.value) {
    updatePlayerRole(
      groupStore.getGroup().id,
      player.id,
      selectedRole.value === player.role ? '' : selectedRole.value,
      selectedStatus.value === player.status ? '' : selectedStatus.value
    ).then(() => {
      console.log('Role updated');
    });
  }

  router.push({ name: 'GroupDetail', params: { id: groupStore.getGroup().id } });
};
</script>

<template>
  <v-form ref="editPlayerForm" @submit.prevent="submit">
    <v-text-field
        v-if="player"
      color="primary"
      v-model="player.name"
      label="Spieler Name"
      readonly
    ></v-text-field>
    <v-select
      v-model="selectedRole"
      :items="['ADMIN', 'PLAYER']"
      label="Rolle"
    ></v-select>
    <v-select
      v-model="selectedStatus"
      :items="['ACTIVE', 'INACTIVE', 'REMOVED', 'LEAVE']"
      label="Status"
    ></v-select>
    <v-btn class="mt-2" type="submit" block>Speichern</v-btn>
  </v-form>
</template>

<style scoped></style>
