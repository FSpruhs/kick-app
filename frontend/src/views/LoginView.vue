<script setup lang="ts">
import { ref } from 'vue'
import axios from 'axios'
import type { VForm } from 'vuetify/components'
import { useRouter } from 'vue-router'
import userService from '@/services/userRestService'

const email = ref('')
const password = ref('')
const loginForm = ref<VForm | null>(null)
const router = useRouter()

const emailRule = ref([
  (v: string) => !!v || 'First name is required',
  (v: string) => (v && v.length <= 40) || 'First name must be less than 40 characters'
])

const passwordRule = ref([
  (v: string) => !!v || 'Last name is required',
  (v: string) => (v && v.length <= 40) || 'Last name must be less than 40 characters'
])
const submit = async () => {
  if (!loginForm.value) return
  const { valid, errors } = await loginForm.value.validate()
  if (!valid) {
    console.log(errors)
    return
  }
  console.log(email.value, password.value)
  await userService.postLogin({ email: email.value, password: password.value })

  loginForm.value.reset()
  await router.push({ name: 'Home' })
}
</script>

<template>
  <v-app>
    <v-container class="d-flex align-center justify-center" style="height: 100vh">
      <v-sheet class="mx-auto pa-8" elevation="12" width="300" border rounded>
        <v-form ref="loginForm" @submit.prevent="submit">
          <v-text-field
            color="primary"
            v-model="email"
            :rules="emailRule"
            label="First name"
          ></v-text-field>

          <v-text-field
            color="primary"
            v-model="password"
            :rules="passwordRule"
            label="Last name"
          ></v-text-field>

          <v-btn class="mt-2" type="submit" block>Submit</v-btn>
        </v-form>
      </v-sheet>
    </v-container>
  </v-app>
</template>

<style scoped></style>
