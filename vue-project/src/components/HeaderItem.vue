<script setup>
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import { computed } from 'vue';

const authStore = useAuthStore();
const router = useRouter();

/*const userRoles = computed(() => {
  const roles = authStore.user?.roles;
  return Array.isArray(roles) ? roles : roles ? [roles] : [];
});*/

const isAdmin = computed(() => {
  return authStore.isAdmin;
});

const isUser = computed(() => {
  return authStore.isAuthenticated && !isAdmin.value;
});

const logout = () => {
  authStore.logout();
  router.push('/');
};

defineProps({
  logoHeight: {
    type: String,
    default: '60px' //Propiedades para ajustar el tamaño en la imagen de manera flexible
  },
  cartHeight: {
    type: String,
    default: '40px'
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


      <input v-if="!isAdmin" type="text" placeholder="Busca algun producto" class="search"/>

    <nav class="nav">
      <template v-if="isAdmin">
        <router-link to="/admin">Panel de Administrador</router-link>
        <button @click="logout" class="btn-logout">Cerrar Sesión</button>
      </template>

      <template v-else-if="isUser">
         <router-link to="/profile">Mi Perfil</router-link>
        <button @click="logout" class="btn-logout">Cerrar Sesión</button>
      </template>

      <template v-else>
        <router-link to="/auth/login">Iniciar Sesión</router-link>
        <router-link to="/auth/register" class="btn-register">Registrarse</router-link>
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

.nav {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.nav a {
  margin-left: 1.5rem;
  color: #000000;
  text-decoration: none;
}
.nav a:hover { color: rgb(63, 63, 63); }

.btn-logout {
  background: none;
  border: 2px solid #ff4d4d;
  border-radius: 999px;
  color: #ff4d4d;
  font-family: inherit;
  margin-left: 1.5rem;
  padding: 0.3rem 0.8rem;
  cursor: pointer;
}

.btn-logout:hover {
  background-color: #ff4d4d;
  color: #ffffff;
}
.btn-register {
  background-color: #3498db;
  color: #000000 !important;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.search {
  flex: 2;
  max-width: 600px;
  height: 40px;
  padding: 0 15px;
  border: 1px solid #ccc;
  border-radius: 4px;
  outline: none;

}

</style>
