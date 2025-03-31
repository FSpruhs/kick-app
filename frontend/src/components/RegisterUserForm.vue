<script setup lang="ts">
import { ref, defineEmits } from 'vue';
import type { VForm } from 'vuetify/components';

const registerForm = ref<VForm | null>(null);
const nickName = ref('');
const email = ref('');

const emits = defineEmits<{
  (event: 'submit', payload: { nickName: string; email: string }): void;
}>();

const nicknameRule = ref([
  (v: string) => !!v || 'Nickname is required',
  (v: string) => (v && v.length <= 40) || 'Nickname must be less than 40 characters'
]);

const emailRule = ref([
  (v: string) => !!v || 'Email is required',
  (v: string) => (v && v.length <= 40) || 'Email must be less than 40 characters'
]);

const submit = async () => {
  if (!registerForm.value) return;
  const { valid } = await registerForm.value.validate();
  if (!valid) return;
  emits('submit', { nickName: nickName.value, email: email.value });
};
</script>

<template>
  <v-form ref="registerForm" @submit.prevent="submit">
    <v-text-field
        color="primary"
        v-model="nickName"
        :rules="nicknameRule"
        label="Nickname"
    ></v-text-field>
    <v-text-field
        color="primary"
        v-model="email"
        :rules="emailRule"
        label="Email"
    ></v-text-field>
    <v-btn class="mt-2" type="submit" block>Registrieren</v-btn>
  </v-form>
</template>
