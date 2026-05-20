<script setup>
import { onMounted } from 'vue'
import { useSubscriptionStore } from '@/stores/subscription'
import SubscribeButton from './SubscribeButton.vue'
import HeaderItem from './HeaderItem.vue'
import FooterItem from './FooterItem.vue'

const subStore = useSubscriptionStore()

onMounted(async () => {
  if (!subStore.plans.length) await subStore.fetchPlans()
})
</script>

<template>
  <div class="plans-container-full">
    <HeaderItem />

    <main class="plans-main">
      <div class="plans-header">
        <h1>Nuestros Planes de Suscripción</h1>
        <p>Elige el plan que mejor se adapte a tu estilo de vida</p>
      </div>

      <div class="plans-grid">
        <div v-for="plan in subStore.plans" :key="plan.id" class="plan-card">
          <div class="plan-header">
            <h3>{{ plan.name }}</h3>
            <p class="plan-price" v-if="plan.price">{{ plan.price === '0' || plan.price === 0 ? 'Gratis' : `€${plan.price}` }}/mes</p>
          </div>

          <p class="plan-description">{{ plan.description }}</p>

          <div class="benefits">
            <h4>Beneficios Incluidos:</h4>
            <ul>
              <li v-if="plan.freeShipping" class="benefit-item">
                <span class="benefit-icon">🚚</span>
                <span>Envío gratis</span>
              </li>
              <li v-if="plan.discountPercentage > 0" class="benefit-item">
                <span class="benefit-icon">💰</span>
                <span>{{ plan.discountPercentage }}% de descuento en compras</span>
              </li>
              <li v-if="!plan.freeShipping && (!plan.discountPercentage || plan.discountPercentage === 0)" class="benefit-item">
                <span class="benefit-icon">⭐</span>
                <span>Acceso a suscripción estándar</span>
              </li>
            </ul>
          </div>

          <SubscribeButton :planId="plan.id" />
        </div>
      </div>

      <div class="plans-comparison">
        <h2>Compara Nuestros Planes</h2>
        <div v-if="subStore.plans.length" class="comparison-table" :style="{ '--num-plans': subStore.plans.length }">
          <div class="table-header">
            <div class="table-cell feature">Característica</div>
            <div class="table-cell" v-for="plan in subStore.plans" :key="plan.id">{{ plan.name }}</div>
          </div>
          <div class="table-row">
            <div class="table-cell feature">Envío Gratis</div>
            <div class="table-cell" v-for="plan in subStore.plans" :key="plan.id">
              <span v-if="plan.freeShipping">✓</span>
              <span v-else>-</span>
            </div>
          </div>
          <div class="table-row">
            <div class="table-cell feature">Descuento en Productos</div>
            <div class="table-cell" v-for="plan in subStore.plans" :key="plan.id">
              <span v-if="plan.discountPercentage > 0">{{ plan.discountPercentage }}%</span>
              <span v-else>-</span>
            </div>
          </div>
          <div class="table-row">
            <div class="table-cell feature">Precio Mensual</div>
            <div class="table-cell" v-for="plan in subStore.plans" :key="plan.id">
              {{ plan.price === '0' || plan.price === 0 ? 'Gratis' : `€${plan.price}` }}
            </div>
          </div>
        </div>
        <div v-else class="loading-table">
          <p>Cargando planes...</p>
        </div>
      </div>
    </main>

    <FooterItem />
  </div>
</template>

<style scoped>
.plans-container-full {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.plans-main {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.plans-header {
  text-align: center;
  margin-bottom: 3rem;
}

.plans-header h1 {
  color: #ffffff;
  font-size: 2.8rem;
  margin-bottom: 1rem;
  font-weight: 700;
}

.plans-header p {
  color: #ccc;
  font-size: 1.1rem;
}

.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.plan-card {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  display: flex;
  flex-direction: column;
}

.plan-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.plan-header {
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 1rem;
  margin-bottom: 1rem;
}

.plan-header h3 {
  color: #667eea;
  font-size: 1.6rem;
  margin: 0 0 0.5rem 0;
}

.plan-price {
  color: #333;
  font-size: 1.4rem;
  font-weight: 700;
  margin: 0;
}

.plan-description {
  color: #666;
  font-size: 0.95rem;
  margin-bottom: 1.5rem;
  line-height: 1.6;
}

.benefits {
  flex: 1;
  margin-bottom: 2rem;
}

.benefits h4 {
  color: #333;
  margin: 0 0 1rem 0;
  font-size: 1rem;
}

.benefits ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.7rem 0;
  color: #555;
  font-size: 0.95rem;
}

.benefit-icon {
  font-size: 1.2rem;
}

.plans-comparison {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.plans-comparison h2 {
  text-align: center;
  color: #333;
  margin-bottom: 2rem;
  font-size: 1.8rem;
}

.comparison-table {
  display: grid;
  gap: 1px;
  background: #ddd;
  border-radius: 8px;
  overflow: hidden;
}

.table-header, .table-row {
  display: grid;
  grid-template-columns: 200px repeat(var(--num-plans, 2), 1fr);
  background: white;
}

.table-header {
  background: #667eea;
  color: white;
  font-weight: 600;
}

.table-cell {
  padding: 1rem;
  text-align: center;
}

.table-cell.feature {
  text-align: left;
  font-weight: 600;
  color: #333;
}

.table-header .table-cell.feature {
  color: white;
}

.table-row:nth-child(even) {
  background: #f9f9f9;
}

.loading-table {
  text-align: center;
  padding: 2rem;
  color: #666;
}

@media (max-width: 768px) {
  .plans-grid {
    grid-template-columns: 1fr;
  }

  .plans-header h1 {
    font-size: 2rem;
  }

  .comparison-table {
    overflow-x: auto;
  }

  .table-header, .table-row {
    grid-template-columns: 150px repeat(var(--num-plans, 2), 1fr);
  }
}
</style>
