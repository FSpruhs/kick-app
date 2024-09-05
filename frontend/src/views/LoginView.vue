<script setup lang="ts">
import { ref } from 'vue';
import type { VForm } from 'vuetify/components';
import { useRouter } from 'vue-router';
import userService from '@/services/userRestService';
import { useUserStore } from '@/store/UserStore';
import type { User } from '@/model/user';

const email = ref('');
const password = ref('');
const loginForm = ref<VForm | null>(null);
const router = useRouter();
const userStore = useUserStore();

const emailRule = ref([
  (v: string) => !!v || 'First name is required',
  (v: string) => (v && v.length <= 40) || 'First name must be less than 40 characters'
]);

const passwordRule = ref([
  (v: string) => !!v || 'Last name is required',
  (v: string) => (v && v.length <= 40) || 'Last name must be less than 40 characters'
]);
const submit = async () => {
  if (!loginForm.value) return;
  const { valid } = await loginForm.value.validate();
  if (!valid) return;

  await userService
    .postLogin({ email: email.value, password: password.value })
    .then((response) => {
      const user: User = {
        firstName: response.data.firstName,
        lastName: response.data.lastName,
        nickname: response.data.nickName,
        email: response.data.email,
        id: response.data.id,
        groups: response.data.groups
      };
      console.log(user);
      userStore.saveUser(user);
      router.push({ name: 'Home' });
      console.log(userStore.getUser());
      if (!loginForm.value) return;
      loginForm.value.reset();
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
            <span class="headline">Login</span>
          </v-card-title>
          <v-card-text>
            <v-form ref="loginForm" @submit.prevent="submit">
              <v-text-field
                color="primary"
                v-model="email"
                :rules="emailRule"
                label="Mailadresse"
              ></v-text-field>

              <v-text-field
                color="primary"
                v-model="password"
                :rules="passwordRule"
                label="Passwort"
              ></v-text-field>
              <v-btn class="mt-2" type="submit" block>Submit</v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-sheet>
    </v-container>
  </v-app>
</template>

<style scoped></style>
