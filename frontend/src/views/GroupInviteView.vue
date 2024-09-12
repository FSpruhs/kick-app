<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
const groupId = router.currentRoute.value.params.id;
</script>

<template>
  <h1>Group Invite to {{ groupId }}</h1>
</template>

<style scoped></style>
