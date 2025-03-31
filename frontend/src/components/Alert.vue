<script setup lang="ts">
import { ref, watch } from 'vue';

const props = defineProps<{
  type: 'success' | 'error' | 'info' | 'warning';
  message: string | null;
}>();

const emit = defineEmits(['update:message']);

const isVisible = ref(false);

watch(() => props.message, (newValue) => {
  if (newValue) {
    isVisible.value = true;
    setTimeout(() => {
      isVisible.value = false;
      emit('update:message', null);
    }, 5000);
  }
});
</script>

<template>
  <v-alert
      v-if="isVisible"
      :type="type"
      closable
      @click:close="emit('update:message', null)"
  >
    {{ props.message }}
  </v-alert>
</template>
