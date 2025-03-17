<script setup lang="ts">
import { useRouter } from 'vue-router';
import { onMounted, ref } from 'vue';
import { getMatch, type MatchResponse } from '@/services/matchRestService';

const router = useRouter();

const matchId = String(router.currentRoute.value.params.id);
const matchDetails = ref<MatchResponse | null>(null);

onMounted(() => {
  getMatch(matchId)
    .then((result) => {
      matchDetails.value = result.data;
    })
    .catch((error) => {
      console.error(error);
    });
});
</script>

<template>
  <div></div>
  <v-app>
    <v-container class="d-flex align-center justify-center">
      <v-sheet>
        <v-card class="pa-4" elevation="12" width="1200" rounded>
          <v-card-title>
            <h1>Match Details</h1>
          </v-card-title>
          <v-card-text>
            <v-card class="my-4 pa-4">
              <v-card-title>
                <h3>Details</h3>
              </v-card-title>
              <v-card-text>
                <v-row>
                  <v-col> Match Id: {{ matchDetails?.matchId }} </v-col>
                  <v-col> Gruppen id: {{ matchDetails?.groupId }} </v-col>
                </v-row>
                <v-row>
                  <v-col> Mindest Spieler: {{ matchDetails?.minPlayer }} </v-col>
                  <v-col> Maximal Spieler: {{ matchDetails?.maxPlayer }} </v-col>
                </v-row>
                <v-row>
                  <v-col> Start: {{ matchDetails?.start }} </v-col>
                  <v-col> Ort: {{ matchDetails?.playground }} </v-col>
                  <v-col> Ort: {{ matchDetails?.status }} </v-col>
                </v-row>
              </v-card-text>
            </v-card>
            <v-card class="my-4 pa-4">
              <v-card-title>
                <h3>Anmeldung</h3>
              </v-card-title>
              <v-card-text>
                <v-row>
                  <v-col>
                    <v-table>
                      <caption>
                        Akzeptierte Spieler: {{ matchDetails?.acceptedPlayers.length }}
                      </caption>
                      <thead>
                        <tr>
                          <th class="text-left">ID</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="item in matchDetails?.acceptedPlayers" :key="item">
                          <td>{{ item }}</td>
                        </tr>
                      </tbody>
                    </v-table>
                  </v-col>
                  <v-col>
                    <v-table>
                      <caption>
                        Abgemeldete Spieler: {{ matchDetails?.deregisteredPlayers.length }}
                      </caption>
                      <thead>
                        <tr>
                          <th class="text-left">ID</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="item in matchDetails?.deregisteredPlayers" :key="item">
                          <td>{{ item }}</td>
                        </tr>
                      </tbody>
                    </v-table>
                  </v-col>
                  <v-col>
                    <v-table>
                      <caption>
                        Wartebank Spieler: {{ matchDetails?.waitingBenchPlayers.length }}
                      </caption>
                      <thead>
                        <tr>
                          <th class="text-left">ID</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="item in matchDetails?.waitingBenchPlayers" :key="item">
                          <td>{{ item }}</td>
                        </tr>
                      </tbody>
                    </v-table>
                  </v-col>
                </v-row>
              </v-card-text>
            </v-card>
            <v-card class="my-4 pa-4">
              <v-card-title>
                <h3>Ergebnis</h3>
              </v-card-title>
              <v-card-text>
                <v-row>
                  <v-col> Result: {{ matchDetails?.result }} </v-col>
                </v-row>
                <v-row>
                  <v-col>
                    <v-table>
                      <caption>
                        Team A
                      </caption>
                      <thead>
                        <tr>
                          <th class="text-left">ID</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="item in matchDetails?.teamA" :key="item">
                          <td>{{ item }}</td>
                        </tr>
                      </tbody>
                    </v-table>
                  </v-col>
                  <v-list> </v-list>
                  <v-col>
                    <v-table>
                      <caption>
                        Team B
                      </caption>
                      <thead>
                        <tr>
                          <th class="text-left">ID</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr v-for="item in matchDetails?.teamB" :key="item">
                          <td>{{ item }}</td>
                        </tr>
                      </tbody>
                    </v-table>
                  </v-col>
                </v-row>
              </v-card-text>
            </v-card>
          </v-card-text>
        </v-card>
      </v-sheet>
    </v-container>
  </v-app>
</template>

<style scoped></style>
