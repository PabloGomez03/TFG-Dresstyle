<script setup>
import FooterItem from '@/components/FooterItem.vue';
import HeaderItem from '@/components/HeaderItem.vue';
import http from '@/api/http';
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
var name = ref('');
var email = ref('');
var password = ref('');

function validateEmails() {
  let email = document.getElementById('email').value;
  let confirmEmail = document.getElementById('confirm-email').value;
  let errorMsg = document.getElementById('errorMsg');

  if (email !== confirmEmail) {
    errorMsg.style.display = 'block';
    return false;
  } else {
    errorMsg.style.display = 'none';
    return true;
  }
}

const registerUser = async () => {
  if (!validateEmails()) {
    return;
  }

  try {
    await http.post('/auth/register', {
      name: name.value,
      email: email.value,
      password: password.value
    });
    alert("¡Registro con éxito!");
    router.push('/login');
  } catch (error) {
    console.error("Error en registro:", error);
    alert("Fallo en el registro: " + (error.response?.data || "Servidor no disponible"));
  }

};
</script>

<template>

<HeaderItem class="main-header"/>

<div class="register-view">

  <div class="content">

    <form class="register-form" @submit.prevent="registerUser">

      <h2>Registrarse</h2>

      <div class="form-group">
        <label for="username">Nombre de Usuario</label>
        <input v-model="name" type="text" id="username" placeholder="Ingresa tu nombre de usuario" required />
      </div>

      <div class="form-group">
        <label for="email">Correo Electrónico</label>
        <input v-model="email" @input="validateEmails" type="email" id="email" placeholder="Ingresa tu correo electrónico" required />
      </div>

      <div class="form-group">
        <label for="email">Confirmar Correo</label>
        <input v-model="confirmEmail" @input="validateEmails" type="email" id="confirm-email" placeholder="Vuelve a ingresarlo" required />

        <div id="errorMsg">
          <i class="wrong-emails">❌Los correos no coinciden</i>
        </div>

      </div>

      <div class="form-group">
        <label for="password">Contraseña</label>
        <input v-model="password" type="password" id="password" placeholder="Crea una contraseña" required />
      </div>

      <button type="submit">Registrarse</button>

    </form>

  </div>

</div>

<FooterItem class="main-footer"/>

</template>

<style scoped>

.register-view {
  display: flex;
  flex-direction: column; /*Ocupa toda la altura de la ventana de home*/
  min-height: 100vh;
}

.content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
}

.main-footer {
  margin-top: auto;
}

.register-form {
  width: 100%;
  max-width: 450px;
  background: white;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
}

.register-form h2 {
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
  color: #b9b9b9;
}

.register-form button {
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

.register-form button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.register-form button:active {
  transform: translateY(0);
}

#errorMsg {
  color: red;
  font-size: 0.75rem;
  display: none;
  margin-top: 0.5rem;
}

@media (max-width: 600px) {
  .register-form {
    padding: 2rem;
  }

  .register-form h2 {
    font-size: 1.5rem;
    margin-bottom: 1.5rem;
  }

  .content {
    padding: 1rem;
  }
}

</style>
