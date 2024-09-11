import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/HomeView.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/registration',
    name: 'Registration',
    component: () => import('../views/RegistrationView.vue')
  },
  {
    path: '/group',
    name: 'Group',
    component: () => import('../views/GroupView.vue')
  },
  {
    path: '/group/:id',
    name: 'GroupDetail',
    component: () => import('../views/GroupDetailView.vue')
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
