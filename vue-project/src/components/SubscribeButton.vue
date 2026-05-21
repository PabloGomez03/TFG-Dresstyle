<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useSubscriptionStore } from '@/stores/subscription'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({ planId: { type: String, required: true } })

const subStore = useSubscriptionStore()
const authStore = useAuthStore()
const router = useRouter()
const loading = ref(false)
const message = ref('')
const messageType = ref('')

const showMessage = (text, type) => {
  message.value = text
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 4000)
}

const subscribe = async () => {
  if (!authStore.isAuthenticated) {
    router.push('/auth/login')
    return
  }
  loading.value = true
  try {
    await subStore.subscribe(props.planId)
    showMessage('Suscripción activada correctamente!', 'success')
    setTimeout(() => {
      router.push('/account/subscription')
    }, 2000)
  } catch (e) {
    console.error(e)
    showMessage('Error al suscribirse. Intenta de nuevo.', 'error')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="subscribe-wrapper">
    <button :disabled="loading || subStore.loading" @click="subscribe" class="btn-subscribe">
      <span v-if="!loading">Suscribirse</span>
      <span v-else>Procesando...</span>
    </button>
    <div v-if="message" :class="['feedback-message', messageType]">
      {{ message }}
    </div>
  </div>
</template>

<style scoped>
.subscribe-wrapper {
  position: relative;
  display: inline-block;
  width: 100%;
}

.btn-subscribe {
  width: 100%;
  background: #667eea;
  color: white;
  border: none;
  padding: 0.7rem 1.2rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.3s ease;
}

.btn-subscribe:hover:not(:disabled) {
  background: #5568d3;
}

.btn-subscribe[disabled] {
  opacity: 0.6;
  cursor: not-allowed;
}

.feedback-message {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 0.5rem;
  padding: 0.7rem;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 600;
  text-align: center;
  animation: slideIn 0.3s ease;
}

.feedback-message.success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.feedback-message.error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
