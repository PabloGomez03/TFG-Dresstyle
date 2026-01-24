<script setup>
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const logout = () => {
  authStore.logout();
  router.push('/login');
};

defineProps({
  logoHeight: {
    type: String,
    default: '60px'
  }
})

</script>

<template>
  <header class="header">
    <div class="logo" >
      <router-link to="/">

        <img src="@/img/logo.png" :style="{height: logoHeight}" class="logo-img" />

      </router-link>
    </div>

    <nav class="nav">
      <router-link to="/">Inicio</router-link>

      <template v-if="authStore.isAuthenticated">
        <router-link to="/perfil">Mi Perfil</router-link>
        <button @click="logout" class="btn-logout">Cerrar Sesión</button>
      </template>

      <template v-else>
        <router-link to="/login">Iniciar Sesión</router-link>
        <router-link to="/registro" class="btn-register">Registrarse</router-link>
      </template>
    </nav>
  </header>
</template>


<style scoped>

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: #ffffff;
  color: rgb(255, 255, 255);
  border-bottom: 3px solid #b9b9b9;
}
.logo a {
  font-size: 1.5rem;
  font-weight: bold;
  color: #fff;
  text-decoration: none;
}
.nav a {
  margin-left: 1.5rem;
  color: #000000;
  text-decoration: none;
}
.nav a:hover { color: rgb(63, 63, 63); }
.btn-logout {
  background: none;
  border: 1px solid #ff4d4d;
  color: #ff4d4d;
  margin-left: 1.5rem;
  padding: 0.3rem 0.8rem;
  cursor: pointer;
}

.btn-logout button:hover {

  color: rgb(73, 73, 73);

}
.btn-register {
  background-color: #3498db;
  color: rgb(0, 0, 0) !important;
  padding: 0.5rem 1rem;
  border-radius: 4px;
}



</style>
