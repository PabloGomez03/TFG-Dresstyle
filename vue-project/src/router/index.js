import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LoginView from '../views/LoginView.vue'
import HomeView from '../views/HomeView.vue'
import RegisterView from '@/views/RegisterView.vue'
import CartView from '@/views/CartView.vue'
import ProfileView from '@/views/ProfileView.vue'
import AdminPanelView from '@/views/AdminPanelView.vue'
import SearchView from '@/views/SearchView.vue'
import ProductDetailView from '@/views/ProductDetailView.vue'
import SubscriptionPlans from '@/components/SubscriptionPlans.vue'
import AccountSubscriptions from '@/views/AccountSubscriptions.vue'
import CheckoutView from '@/views/CheckoutView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path:'/',
      name:'home',
      component: HomeView,

    },
    {
      path: '/auth/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/auth/register',
      name: 'register',
      component: RegisterView

    },
    {
      path:'/cart',
      name:'cart',
      component: CartView,

    },
    {
      path:'/checkout',
      name:'checkout',
      component: CheckoutView,
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: ProfileView,
      meta: { requiresAuth: true }

    },
    {
      path: '/admin',
      name: 'admin-panel',
      component: AdminPanelView,
      meta: { requiresAuth: true, requiresAdmin: true }

    },
    {
      path: '/search',
      name: 'search',
      component: SearchView
    },
    {
      path: '/catalog',
      name: 'catalog',
      component: SearchView
    },
    {
      path: '/product/:id',
      name: 'product-detail',
      component: ProductDetailView
    },
    {
      path: '/subscriptions',
      name: 'subscriptions',
      component: SubscriptionPlans
    },
    {
      path: '/account/subscription',
      name: 'account-subscription',
      component: AccountSubscriptions,
      meta: { requiresAuth: true }
    }
  ]
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  authStore.initializeAuth()

  const isLoggedIn = authStore.isAuthenticated && authStore.isTokenValid
  const isAdmin = authStore.isAdmin

  if (to.meta.requiresAuth && !isLoggedIn) {
    return { path: '/auth/login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requiresAdmin && !isAdmin) {
    return { path: '/' }
  }

  return true
})

export default router
