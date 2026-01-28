import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import HomeView from '../views/HomeView.vue'
import RegisterView from '@/views/RegisterView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path:'/',
      name:'home',
      component: HomeView
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

    }
    // Add more routes here later
  ]
})

export default router
