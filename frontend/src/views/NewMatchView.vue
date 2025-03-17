<script setup lang="ts">

import {ref} from "vue";

const playground = ref('');
const maxPlayer = ref(0);
const minPlayer = ref(0);
const date = ref('');
const time = ref('');


</script>

<template>
  <v-app>
    <v-container class="d-flex align-center justify-center">
      <v-sheet>
        <v-card>
          <v-card-title>
            <h1>Neues Match</h1>
          </v-card-title>
          <v-card-text>
            <v-form>
              <v-text-field
                  color="primary"
                  v-model="date"
                  label="Spieltag"
                  type="date"
              ></v-text-field>
              <v-text-field
                  color="primary"
                  v-model="time"
                  label="Uhrzeit"
                  type="time"
              ></v-text-field>
              <v-text-field
                  color="primary"
                  v-model="playground"
                  label="Spielort"
              ></v-text-field>
              <v-text-field
                  color="primary"
                  v-model="maxPlayer"
                  label="Maximale Anzahl Spieler"
                  type="number"
              ></v-text-field>
              <v-text-field
                  color="primary"
                  v-model="minPlayer"
                  label="Minimale Anzahl Spieler"
                  type="number"
              ></v-text-field>
            </v-form>
          </v-card-text>
        </v-card>
      </v-sheet>
    </v-container>
  </v-app>
</template>

<style scoped></style>
