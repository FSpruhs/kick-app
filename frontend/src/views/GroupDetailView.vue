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
  <div>
    <h1>GroupDetailView</h1>
    <p>Group ID: {{ groupId }}</p>
    <p>Group Name: {{ groupDetail?.name }}</p>
  </div>
</template>

<style scoped></style>
