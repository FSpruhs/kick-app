<script setup lang="ts">
import { postRegister } from '@/services/userRestService';
import { useRouter } from 'vue-router';
import RegisterUserForm from '@/components/RegisterUserForm.vue';
import { ref } from 'vue';
import Alert from '@/components/Alert.vue';

const router = useRouter();
const errorMessage = ref<string | null>(null);

const handleSubmit = async (payload: { nickName: string; email: string }) => {
  await postRegister(payload)
    .then(() => {
      router.push({ name: 'Index' });
    })
    .catch((error) => {
      console.error(error);
      errorMessage.value = error.response?.data?.errorMessage || 'Registrierung fehlgeschlagen';
    });
};
</script>

<template>
  <v-app>
    <v-container class="d-flex align-center justify-center">
      <v-sheet>
        <v-card class="mx-auto pa-6" elevation="12" width="300" rounded>
          <v-card-title>
            <span class="headline">Registrieren</span>
          </v-card-title>
          <v-card-text>
            <Alert v-model:message="errorMessage" type="error" />
            <RegisterUserForm @submit="handleSubmit" />
          </v-card-text>
        </v-card>
      </v-sheet>
    </v-container>
  </v-app>
</template>

<style scoped></style>
