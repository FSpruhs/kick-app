import { createRouter, createWebHistory } from 'vue-router';
import { useKeycloak } from '@josempgon/vue-keycloak';

const keycloak = useKeycloak();

const routes = [
  {
    path: '/',
    name: 'Index',
    component: () => import('../views/IndexView.vue'),
    meta: {
      requiresAuth: false
    }
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/HomeView.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/registration',
    name: 'Registration',
    component: () => import('../views/RegistrationView.vue'),
    meta: {
      requiresAuth: false
    }
  },
  {
    path: '/group',
    name: 'Group',
    component: () => import('../views/GroupView.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/group/:id',
    name: 'GroupDetail',
    component: () => import('../views/GroupDetailView.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/group/:id/invite',
    name: 'GroupInvite',
    component: () => import('../views/GroupInviteView.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/mailbox',
    name: 'Mailbox',
    component: () => import('../views/MailboxView.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/player/:id',
    name: 'EditPlayer',
    component: () => import('../views/EditPlayerView.vue'),
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/match/new',
    name: 'NewMatch',
    component: () => import('../views/NewMatchView.vue'),
    meta: {
      requiresAuth: true
    }
  }
];

const initRouter = () => {
  const history = createWebHistory(import.meta.env.BASE_URL);
  const router = createRouter({ history, routes });

  router.beforeEach(async (to, from, next) => {
    if (to.meta.requiresAuth && !keycloak.isAuthenticated.value) {
      await keycloak.keycloak.value?.login({ redirectUri: window.location.origin + to.fullPath });
    } else {
      next();
    }
  });

  return router;
};

export default initRouter;
