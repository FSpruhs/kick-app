<script setup lang="ts">
import { ref } from 'vue';
import type { VForm } from 'vuetify/components';
import { postRegister } from '@/services/userRestService';
import { useRouter } from 'vue-router';

const router = useRouter();
const registerForm = ref<VForm | null>(null);
const firstName = ref('');
const lastName = ref('');
const nickname = ref('');
const email = ref('');
const password = ref('');

const firstNameRule = ref([
  (v: string) => !!v || 'First name is required',
  (v: string) => (v && v.length <= 40) || 'First name must be less than 40 characters'
]);

const lastNameRule = ref([
  (v: string) => !!v || 'Last name is required',
  (v: string) => (v && v.length <= 40) || 'Last name must be less than 40 characters'
]);

const nicknameRule = ref([
  (v: string) => !!v || 'Nickname is required',
  (v: string) => (v && v.length <= 40) || 'Nickname must be less than 40 characters'
]);

const emailRule = ref([
  (v: string) => !!v || 'Email is required',
  (v: string) => (v && v.length <= 40) || 'Email must be less than 40 characters'
]);

const passwordRule = ref([
  (v: string) => !!v || 'Password is required',
  (v: string) => (v && v.length <= 40) || 'Password must be less than 40 characters'
]);

const submit = async () => {
  if (!registerForm.value) return;
  const { valid } = await registerForm.value.validate();
  if (!valid) return;
  const payload = {
    firstName: firstName.value,
    lastName: lastName.value,
    nickName: nickname.value,
    email: email.value,
    password: password.value
  };
  await postRegister(payload)
    .then((response) => {
      console.log(response);
      router.push({ name: 'Login' });
    })
    .catch((error) => {
      console.log(error);
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
            <v-form ref="registerForm" @submit.prevent="submit">
              <v-text-field
                color="primary"
                v-model="firstName"
                :rules="firstNameRule"
                label="Vorname"
              ></v-text-field>
              <v-text-field
                color="primary"
                v-model="lastName"
                :rules="lastNameRule"
                label="Nachname"
              ></v-text-field>
              <v-text-field
                color="primary"
                v-model="nickname"
                :rules="nicknameRule"
                label="Nickname"
              ></v-text-field>
              <v-text-field
                color="primary"
                v-model="email"
                :rules="emailRule"
                label="Email"
              ></v-text-field>
              <v-text-field
                color="primary"
                v-model="password"
                :rules="passwordRule"
                label="Password"
              ></v-text-field>
              <v-btn class="mt-2" type="submit" block>Registrieren</v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-sheet>
    </v-container>
  </v-app>
</template>

<style scoped></style>
