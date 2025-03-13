<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";
import {getMatch, type MatchResponse} from "@/services/matchRestService";
import App from "@/App.vue";

const router = useRouter();

const matchId = String(router.currentRoute.value.params.id);
const matchDetails = ref<MatchResponse | null>(null);

onMounted(() => {
  getMatch(matchId).then((result) => {
    matchDetails.value = result.data
  }).
  catch((error) => {
    console.error(error)
  })
})
</script>

<template>
  <div>

  </div>
  <v-app>
    <v-container>
      <v-sheet>
        <v-card-title>
          <h1>Match Details</h1>
        </v-card-title>
        <v-card-text>
          <v-row>
            <v-col>
             Match Id: {{matchDetails?.matchId}}
            </v-col>
            <v-col>
              Gruppen id: {{matchDetails?.groupId}}
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              Mindest Spieler: {{matchDetails?.minPlayer}}
            </v-col>
            <v-col>
              Maximal Spieler: {{matchDetails?.maxPlayer}}
            </v-col>
          </v-row>
          <v-row>
            <v-col>
              Start: {{matchDetails?.start}}
            </v-col>
            <v-col>
              Ort: {{matchDetails?.playground}}
            </v-col>
            <v-col>
              Ort: {{matchDetails?.status}}
            </v-col>
          </v-row>
          <v-row>
            <v-table>
              <caption>Akzeptierte Spieler</caption>
              <thead>
              <tr>
                <th class="text-left">ID</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="item in matchDetails?.acceptedPlayers" :key="item">
                <td>{{ item}}</td>
              </tr>
              </tbody>
            </v-table>
          </v-row>
          <v-row>
            <v-table>
              <caption>Abgemeldete Spieler</caption>
              <thead>
              <tr>
                <th class="text-left">ID</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="item in matchDetails?.deregisteredPlayers" :key="item">
                <td>{{ item}}</td>
              </tr>
              </tbody>
            </v-table>
          </v-row>
          <v-row>
            <v-table>
              <caption>Wartebank Spieler</caption>
              <thead>
              <tr>
                <th class="text-left">ID</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="item in matchDetails?.waitingBenchPlayers" :key="item">
                <td>{{ item}}</td>
              </tr>
              </tbody>
            </v-table>
          </v-row>
          <v-row>
            Result: {{matchDetails?.result}}
          </v-row>
          <v-row>
            <v-table>
              <caption>Team A</caption>
              <thead>
              <tr>
                <th class="text-left">ID</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="item in matchDetails?.teamA" :key="item">
                <td>{{ item}}</td>
              </tr>
              </tbody>
            </v-table>
          </v-row>
          <v-row>
            <v-table>
              <caption>Team B</caption>
              <thead>
              <tr>
                <th class="text-left">ID</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="item in matchDetails?.teamB" :key="item">
                <td>{{ item}}</td>
              </tr>
              </tbody>
            </v-table>
          </v-row>
        </v-card-text>
      </v-sheet>
    </v-container>
  </v-app>
</template>

<style scoped>

</style>