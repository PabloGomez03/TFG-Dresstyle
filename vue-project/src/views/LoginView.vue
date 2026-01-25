<script setup>
import HeaderItem from '@/components/HeaderItem.vue'
import FooterItem from '@/components/FooterItem.vue';

</script>


<template>

<div class="login-view">

  <HeaderItem class="main-header"/>

  <div class="content">

    <form class="login-form" action ="/login" method="POST">

      <h2>Iniciar Sesión</h2>
      <div class="form-group">
        <label for="email">Correo Electrónico</label>
        <input type="email" id="email" placeholder="Ingresa tu correo electrónico" required />
      </div>
      <div class="form-group">
        <label for="password">Contraseña</label>
        <input type="password" id="password" placeholder="Ingresa tu contraseña" required />
      </div>
      <button type="submit">Iniciar Sesión</button>
    </form>




  </div>

  <FooterItem  class="main-footer"/>

  </div>

</template>

<style scoped>
.login-view {
  display: flex;
  flex-direction: column; /*Ocupa toda la altura de la ventana de home*/
  min-height: 100vh;
}

.main-footer {
  /* Empuja el componente al final del contenedor */
  margin-top: auto;
}

.content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
}

.login-form {
  width: 100%;
  max-width: 400px;
  background: white;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
}

.login-form h2 {
  text-align: center;
  color: #333;
  font-size: 1.8rem;
  margin-bottom: 2rem;
  font-weight: 600;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
  font-size: 0.95rem;
}

.form-group input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1.5px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  background-color: #f9f9ff;
}

.form-group input::placeholder {
  color: #999;
}

.login-form button {
  width: 100%;
  padding: 0.875rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 1.5rem;
}

.login-form button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.login-form button:active {
  transform: translateY(0);
}

@media (max-width: 600px) {
  .login-form {
    padding: 2rem;
  }

  .login-form h2 {
    font-size: 1.5rem;
    margin-bottom: 1.5rem;
  }

  .content {
    padding: 1rem;
  }
}

</style>
