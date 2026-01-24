<script setup>
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const logout = () => {
  authStore.logout();
  router.push('/login');
};

</script>

<template>
  <header class="header">
    <div class="logo">
      <router-link to="/">DresStyle</router-link>
    </div>

    <nav class="nav">
      <router-link to="/">Inicio</router-link>
      <router-link to="/catalogo">Catálogo</router-link>

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
  background-color: #1a1a1a;
  color: white;
}
.logo a {
  font-size: 1.5rem;
  font-weight: bold;
  color: #fff;
  text-decoration: none;
}
.nav a {
  margin-left: 1.5rem;
  color: #ccc;
  text-decoration: none;
}
.nav a:hover { color: white; }
.btn-logout {
  background: none;
  border: 1px solid #ff4d4d;
  color: #ff4d4d;
  margin-left: 1.5rem;
  padding: 0.3rem 0.8rem;
  cursor: pointer;
}
.btn-register {
  background-color: #3498db;
  color: white !important;
  padding: 0.5rem 1rem;
  border-radius: 4px;
}
</style>
