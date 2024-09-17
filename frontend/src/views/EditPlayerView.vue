<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
const playerId = router.currentRoute.value.params.id;
</script>

<template>
  <h1>Edit Player {{ playerId }}</h1>
</template>

<style scoped></style>
