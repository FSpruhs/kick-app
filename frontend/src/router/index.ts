import { createRouter, createWebHistory } from 'vue-router';
import keycloak from '@/services/keycloakService';
import { useAuthStore } from '@/store/authStore';
import { useUserStore } from '@/store/UserStore';

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

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !keycloak.authenticated) {
    keycloak.login().then((authenticated) => {
      console.log(keycloak.clientId);
      if (authenticated) {
        const authStore = useAuthStore();
        const userStore = useUserStore();

        authStore.setAuthData({
          token: keycloak.token ?? '',
          refreshToken: keycloak.refreshToken ?? '',
          userName: keycloak.tokenParsed?.preferred_username ?? '',
          userId: keycloak.tokenParsed?.sub ?? '',
          roles: keycloak.tokenParsed?.realm_access?.roles ?? [],
          email: keycloak.tokenParsed?.email ?? '',
          authenticated: true
        });

        userStore.saveUser({
          id: keycloak.tokenParsed?.sub ?? '',
          firstName: keycloak.tokenParsed?.given_name ?? '',
          nickname: keycloak.tokenParsed?.preferred_username ?? '',
          lastName: keycloak.tokenParsed?.family_name ?? '',
          email: keycloak.tokenParsed?.email ?? '',
          groups: []
        });
      }
    });
  } else {
    console.log('4');
    next();
  }
});

export default router;
