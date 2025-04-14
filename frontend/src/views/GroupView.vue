<script setup lang="ts">
import type { VForm } from 'vuetify/components';
import { ref } from 'vue';
import { postGroup } from '@/services/groupRestService';
import { useRouter } from 'vue-router';
import {useAuthStore} from "@/store/AuthStore";

const authStore = useAuthStore();
const router = useRouter();
const groupForm = ref<VForm | null>(null);
const groupName = ref('');
const groupNameRule = ref([
  (v: string) => !!v || 'Name is required',
  (v: string) => (v && v.length <= 40) || 'Name must be less than 40 characters'
]);

const submit = async () => {
  if (!groupForm.value) return;
  const { valid } = await groupForm.value.validate();
  if (!valid) return;
  const payload = {
    name: groupName.value,
    userId: authStore.getUserId()
  };
  await postGroup(payload)
    .then((response) => {
      console.log(response);
      router.push({ name: 'Home' });
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
            <span class="headline">Neue Gruppe</span>
          </v-card-title>
          <v-card-text>
            <v-form ref="groupForm" @submit.prevent="submit">
              <v-text-field
                color="primary"
                v-model="groupName"
                :rules="groupNameRule"
                label="Gruppen name"
              ></v-text-field>
              <v-btn class="mt-2" type="submit" block>Erstellen</v-btn>
            </v-form>
          </v-card-text>
        </v-card>
      </v-sheet>
    </v-container>
  </v-app>
</template>

<style scoped></style>
