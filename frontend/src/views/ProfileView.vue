<script setup lang="ts">
import { useAuthStore } from '@/store/AuthStore';
import Alert from '@/components/Alert.vue';
import { ref } from 'vue';
import type { VForm } from 'vuetify/components';
import { updateNickname } from '@/services/userRestService';

const authStore = useAuthStore();
const errorMessage = ref<string | null>(null);

const registerForm = ref<VForm | null>(null);
const nickName = ref(authStore.getUserName());
const email = ref(authStore.getEmail());

const nicknameRule = ref([
  (v: string) => !!v || 'Nickname is required',
  (v: string) => (v && v.length <= 40) || 'Nickname must be less than 40 characters'
]);

const submit = async () => {
  if (!registerForm.value) return;
  if (nickName.value != authStore.getUserName()) {
    await updateNickname(authStore.getUserId(), nickName.value)
      .then()
      .catch((error) => {
        console.error(error);
        errorMessage.value = error.response?.data?.errorMessage || 'Änderung fehlgeschlagen';
      });
  }
};
</script>

<template>
  <v-app>
    <v-container class="d-flex align-center justify-center">
      <v-sheet>
        <v-card class="mx-auto pa-6" elevation="12" width="300" rounded>
          <v-card-title>
            <span class="headline">Profil</span>
          </v-card-title>
          <v-card-text>
            <Alert v-model:message="errorMessage" type="error" />
            <v-form ref="registerForm" @submit.prevent="submit">
              <v-text-field
                color="primary"
                v-model="nickName"
                :rules="nicknameRule"
                label="Nickname"
              ></v-text-field>
              <v-text-field disabled color="primary" v-model="email" label="Email"></v-text-field>
              <v-btn class="mt-2" type="submit" block>Ändern</v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-sheet>
    </v-container>
  </v-app>
</template>

<style scoped></style>
