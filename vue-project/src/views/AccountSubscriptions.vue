<script setup>
import { onMounted, computed } from 'vue'
import { useSubscriptionStore } from '@/stores/subscription'
import HeaderItem from '@/components/HeaderItem.vue'
import FooterItem from '@/components/FooterItem.vue'

const subStore = useSubscriptionStore()

onMounted(async () => {
  await subStore.fetchPlans()
  await subStore.fetchUserSubscription({ force: true })
})

const currentPlan = computed(() => {
  if (!subStore.userSubscription) return null
  return subStore.userSubscription.plan || subStore.plans.find(p => p.id === subStore.userSubscription.plan?.id)
})
</script>

<template>
  <div class="account-subscriptions-container">
    <HeaderItem />

    <main class="account-main">
      <div class="subscription-section">
        <h1>Mi Suscripción</h1>

        <div v-if="!subStore.userSubscription" class="no-subscription">
          <div class="empty-state">
            <h2>No tienes suscripción activa</h2>
            <p>Accede a beneficios exclusivos suscribiéndote a uno de nuestros planes</p>
            <router-link to="/subscriptions" class="btn-view-plans">Ver Planes Disponibles</router-link>
          </div>
        </div>

        <div v-else class="active-subscription">
          <div class="plan-info">
            <h2>{{ currentPlan?.name || 'Suscripción Activa' }}</h2>
            <p class="plan-description">{{ currentPlan?.description }}</p>
          </div>

          <div class="subscription-details">
            <div class="detail-card">
              <h3>Información de la Suscripción</h3>
              <ul>
                <li><strong>Plan:</strong> {{ currentPlan?.name || 'Desconocido' }}</li>
                <li v-if="subStore.userSubscription.startDate"><strong>Activa desde:</strong> {{ new Date(subStore.userSubscription.startDate).toLocaleDateString() }}</li>
                <li v-if="subStore.userSubscription.endDate"><strong>Vencimiento:</strong> {{ new Date(subStore.userSubscription.endDate).toLocaleDateString() }}</li>
              </ul>
            </div>

            <div class="benefits-card">
              <h3>Beneficios Incluidos</h3>
              <ul v-if="currentPlan">
                <li v-if="currentPlan.freeShipping"> Envío gratis en todas tus compras</li>
                <li v-if="currentPlan.discountPercentage"> {{ currentPlan.discountPercentage }}% de descuento en todos los productos</li>
              </ul>
              <p v-else>Disfruta los beneficios de tu plan</p>
            </div>
          </div>

          <div class="actions">
            <router-link to="/subscriptions" class="btn-change-plan">Ver Otros Planes</router-link>
            <button @click="subStore.unsubscribe()" :disabled="subStore.loading" class="btn-cancel">
              <span v-if="!subStore.loading">Cancelar Suscripción</span>
              <span v-else>Cancelando...</span>
            </button>
          </div>
        </div>
      </div>
    </main>

    <FooterItem />
  </div>
</template>

<style scoped>
.account-subscriptions-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.account-main {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.subscription-section h1 {
  color: #ffffff;
  font-size: 2.5rem;
  margin-bottom: 2rem;
  text-align: center;
}

.no-subscription .empty-state {
  background: white;
  padding: 3rem;
  border-radius: 12px;
  text-align: center;
  max-width: 600px;
  margin: 0 auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.empty-state h2 {
  color: #333;
  font-size: 1.8rem;
  margin-bottom: 1rem;
}

.empty-state p {
  color: #666;
  font-size: 1.1rem;
  margin-bottom: 2rem;
}

.btn-view-plans {
  display: inline-block;
  background: #667eea;
  color: white;
  padding: 0.8rem 2rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  transition: background 0.3s ease;
}

.btn-view-plans:hover {
  background: #5568d3;
}

.active-subscription {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-width: 800px;
  margin: 0 auto;
}

.plan-info {
  text-align: center;
  margin-bottom: 2rem;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 1.5rem;
}

.plan-info h2 {
  color: #667eea;
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.plan-description {
  color: #666;
  font-size: 1rem;
}

.subscription-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin: 2rem 0;
}

.detail-card, .benefits-card {
  background: #f9f9f9;
  padding: 1.5rem;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.detail-card h3, .benefits-card h3 {
  color: #333;
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.detail-card ul, .benefits-card ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.detail-card li, .benefits-card li {
  padding: 0.5rem 0;
  color: #555;
  line-height: 1.6;
}

.benefits-card li:before {
  content: '✓ ';
  color: #4caf50;
  font-weight: bold;
  margin-right: 0.5rem;
}

.actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
  justify-content: center;
}

.btn-change-plan, .btn-cancel {
  padding: 0.8rem 2rem;
  border-radius: 8px;
  font-weight: 600;
  text-decoration: none;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-block;
}

.btn-change-plan {
  background: #667eea;
  color: white;
}

.btn-change-plan:hover {
  background: #5568d3;
}

.btn-cancel {
  background: #ff4d4d;
  color: white;
}

.btn-cancel:hover:not(:disabled) {
  background: #e63946;
}

.btn-cancel:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .subscription-details {
    grid-template-columns: 1fr;
  }

  .actions {
    flex-direction: column;
  }

  .btn-change-plan, .btn-cancel {
    width: 100%;
  }
}
</style>
