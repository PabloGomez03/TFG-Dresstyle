<script setup>
import FooterItem from '@/components/FooterItem.vue'
import HeaderItem from '@/components/HeaderItem.vue'
import http from '@/api/http'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
var name = ref('')
var email = ref('')
var confirmEmail = ref('')
var password = ref('')
const showSuccessModal = ref(false)
const isLoading = ref(false)

function validateEmails() {
  let email = document.getElementById('email').value
  let confirmEmail = document.getElementById('confirm-email').value
  let errorMsg = document.getElementById('emailMismatchMsg')
  let duplicateEmailMsg = document.getElementById('duplicateEmailMsg')

  if (duplicateEmailMsg) {
    duplicateEmailMsg.style.display = 'none'
  }

  if (email !== confirmEmail) {
    errorMsg.style.display = 'block'
    return false
  } else {
    errorMsg.style.display = 'none'
    return true
  }
}

function validatePassword() {

  let password = document.getElementById('password').value
  let passwordError = document.getElementById('passwordErrorMsg')

  if(password.length === 0) {
    passwordError.style.display = 'none'
    return true
  }

  if (password.length < 8) {
    passwordError.style.display = 'block'
    return false
  } else {
    passwordError.style.display = 'none'
    return true
  }

}

function successfulRegister() {
  showSuccessModal.value = true
}

const registerUser = async () => {
  if (isLoading.value) {
    return
  }

  if (!validateEmails()) {
    return
  }

  if (!validatePassword()) {
    return
  }

  isLoading.value = true

  try {
    await http.post('/auth/register', {
      name: name.value,
      email: email.value,
      password: password.value,
    })
    successfulRegister()
  } catch (error) {
    if (error.response) {

        console.error("Data del error:", error.response.data);
        console.error("Status HTTP:", error.response.status);

        if(error.response.status === 403) {

          let dupMsg = document.getElementById('duplicateEmailMsg');
          dupMsg.style.display = 'block';

        }

    } else if (error.request) {
        console.error("No hay respuesta del servidor", error.request);
    } else {
        console.error("Error en Axios", error.message);
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="register-page" :class="{ loading: isLoading }">
    <HeaderItem class="main-header" />

    <div class="register-view" :class="{ blurred: showSuccessModal }">
      <div class="content">
        <form class="register-form" @submit.prevent="registerUser">
          <h2>Registrarse</h2>

          <div class="form-group">
            <label for="username">Nombre de Usuario</label>
            <input
              v-model="name"
              type="text"
              id="username"
              placeholder="Ingresa tu nombre de usuario"
              required
            />
          </div>

          <div class="form-group">
            <label for="email">Correo Electrónico</label>
            <input
              v-model="email"
              @input="validateEmails"
              type="email"
              id="email"
              placeholder="Ingresa tu correo electrónico"
              required
            />
            <div id="duplicateEmailMsg" class="error-msg">
              <i class="dup-emails">Ya existe una cuenta con este correo electrónico en el sistema</i>
            </div>
          </div>

          <div class="form-group">
            <label for="confirm-email">Confirmar Correo</label>
            <input
              v-model="confirmEmail"
              @input="validateEmails"
              type="email"
              id="confirm-email"
              placeholder="Vuelve a ingresarlo"
              required
            />

            <div id="emailMismatchMsg" class="error-msg">
              <i class="wrong-emails">Los correos no coinciden</i>
            </div>
          </div>

          <div class="form-group">
            <label for="password">Contraseña</label>
            <input
              v-model="password"
              type="password"
              @input="validatePassword"
              id="password"
              placeholder="Crea una contraseña"
              minlength="8"
              required
            />
          </div>

          <div id="passwordErrorMsg" class="error-msg">
            <i class="weak-password">La contraseña debe tener al menos 8 caracteres</i>
          </div>

          <button type="submit" :disabled="isLoading">Registrarse</button>
        </form>
      </div>
    </div>

    <FooterItem class="main-footer" />

    <div v-if="showSuccessModal" class="success-view" role="dialog" aria-modal="true">
      <div class="success-modal">
        <h3>✅ Registro exitoso</h3>
        <p>Tu cuenta se ha creado correctamente.</p>
        <button type="button" class="login-redirect" @click="router.push('/auth/login')">
          Ir al Login
        </button>
      </div>
    </div>

  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
}

.register-page.loading,
.register-page.loading * {
  cursor: wait !important;
}

.register-view {
  display: flex;
  flex-direction: column; /*Ocupa toda la altura de la ventana de home*/
  min-height: 100vh;
  transition: filter 0.3s ease;
}

.register-view.blurred {
  filter: blur(4px);
  pointer-events: none;
  user-select: none;
}

.success-view {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(24, 24, 37, 0.35);
  backdrop-filter: blur(3px);
  z-index: 999;
}

.success-modal {
  width: min(92vw, 420px);
  background: #fff;
  border-radius: 16px;
  padding: 2rem;
  text-align: center;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.25);
}

.success-modal h3 {
  margin: 0 0 0.6rem;
  color: #2d3748;
  font-size: 1.5rem;
}

.success-modal p {
  margin: 0;
  color: #4a5568;
}

.login-redirect {
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

.login-redirect:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
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



.error-msg {
  color: red;
  font-size: 0.75rem;
  display: none;
  margin-top: 0.25rem;
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
