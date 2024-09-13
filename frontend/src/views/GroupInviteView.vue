<script setup lang="ts">
import { useRouter } from 'vue-router';
import { onMounted, ref } from 'vue';
import { getUserAll, type UserInfo } from '@/services/userRestService';

const router = useRouter();
const groupId = router.currentRoute.value.params.id;
const users = ref<UserInfo[], null>(null);

onMounted(() => {
  getUserAll(groupId).then((response) => {
    users.value = response.data;
    console.log(response.data);
  });
});
</script>

<template>
  <h1>Group Invite to {{ groupId }}</h1>

  <v-table>
    <thead>
      <tr>
        <th class="text-left">ID</th>
        <th class="text-left">Mail</th>
        <th class="text-left">Name</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="item in users" :key="item.id">
        <td>{{ item.id }}</td>
        <td>{{ item.email }}</td>
        <td>{{ item.nickName }}</td>
      </tr>
    </tbody>
  </v-table>
</template>

<style scoped></style>
