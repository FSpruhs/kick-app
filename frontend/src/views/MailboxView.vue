<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getUserMessages, type Message } from '@/services/messageRestService';
import { useUserStore } from '@/store/UserStore';

const mails = ref<Message>([]);
const userStore = useUserStore();

onMounted(() => {
  getUserMessages(userStore.getUser().id).then((response) => {
    mails.value = response.data;
  });
});
</script>

<template>
  <v-container>
    <v-row>
      <!-- Mail Liste auf der linken Seite -->
      <v-col cols="4">
        <v-list dense>
          <v-list-item v-for="(mail, index) in mails" :key="mail.id" @click="selectMail(index)">
            <v-list-item-title :class="{ 'font-weight-bold': !mail.read }">
              {{ mail.subject }}
            </v-list-item-title>
            <v-list-item-subtitle>
              {{ mail.sender }}
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-col>

      <!-- Mail Inhalt auf der rechten Seite -->
      <v-col cols="8">
        <div v-if="selectedMail">
          <h3>{{ selectedMail.subject }}</h3>
          <p><strong>From:</strong> {{ selectedMail.sender }}</p>
          <p>{{ selectedMail.body }}</p>
        </div>
        <div v-else>
          <p>Wähle eine E-Mail aus</p>
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped></style>
