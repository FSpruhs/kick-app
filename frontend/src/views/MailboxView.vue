<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { getUserMessages, readMessage } from '@/services/messageRestService';
import { useMessageStore } from '@/store/MessageStore';
import { responseToGroupInvitation } from '@/services/groupRestService';
import {useAuthStore} from "@/store/AuthStore";

const authStore = useAuthStore();
const messageStore = useMessageStore();
const selectedMailIndex = ref<number | null>(null);

onMounted(() => {
  getUserMessages(authStore.getUserId()).then((response) => {
    messageStore.setMessages(response.data);
  });
});

const sendInvitationResponse = (accept: boolean) => {
  responseToGroupInvitation({
    groupId: selectedMail.value?.groupId ?? '',
    userId: authStore.getUserId(),
    accepted: accept
  }).then(() => {
    console.log('Invitation response sent: ' + accept);
  });
};

const selectedMail = computed(() => {
  if (selectedMailIndex.value !== null) {
    return messageStore.getMessages()[selectedMailIndex.value];
  }
  return null;
});

function selectMail(index: number) {
  selectedMailIndex.value = index;
  const message = messageStore.getMessages()[selectedMailIndex.value];
  if (message.read === false) {
    message.read = true;
    readMessage(message.id)
      .then(() => {
        console.log('Message read: ' + message.id);
      })
      .catch((error) => {
        console.error('Error reading message: ' + error);
      });
  }
}
</script>

<template>
  <v-container>
    <v-row>
      <v-col cols="4">
        <v-list dense>
          <v-list-item
            v-for="(mail, index) in messageStore.getMessages()"
            :key="mail.id"
            @click="selectMail(index)"
          >
            <v-list-item-title :class="{ 'font-weight-bold': !mail.read }">
              {{ mail.type }}
            </v-list-item-title>
            <v-list-item-subtitle>
              {{ mail.content }}
            </v-list-item-subtitle>
          </v-list-item>
        </v-list>
      </v-col>

      <v-col cols="8">
        <div v-if="selectedMail">
          <h3>{{ selectedMail.type }}</h3>
          <p><strong>From:</strong> {{ selectedMail.type }}</p>
          <p>{{ selectedMail.content }}</p>
          <div v-if="selectedMail.type === 'groupInvitation'">
            <v-btn @click="sendInvitationResponse(true)">Annehmen</v-btn>
            <v-btn @click="sendInvitationResponse(false)">Ablehnen</v-btn>
          </div>
        </div>
        <div v-else>
          <p>Wähle eine E-Mail aus</p>
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>

<style scoped></style>
