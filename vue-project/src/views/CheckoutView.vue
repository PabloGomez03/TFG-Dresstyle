<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useSubscriptionStore } from '@/stores/subscription'
import http from '@/api/http'
import HeaderItem from '@/components/HeaderItem.vue'
import FooterItem from '@/components/FooterItem.vue'

const router = useRouter()
const cartStore = useCartStore()
const subStore = useSubscriptionStore()

const shippingAddress = ref({
  street: '',
  city: '',
  state: '',
  zipCode: '',
  country: ''
})

const error = ref('')
const loading = ref(false)
const userAddresses = ref([])
const selectedAddressIndex = ref(0)
const loadingAddresses = ref(true)

onMounted(async () => {
  cartStore.loadCartFromStorage()
  if (cartStore.items.length === 0) {
    router.push('/cart')
    return
  }

  await subStore.fetchPlans()
  await subStore.fetchUserSubscription()

  try {
    const res = await http.get('/auth/profile')
    const addresses = Array.isArray(res.data.addresses) ? res.data.addresses : []
    userAddresses.value = addresses

    if (addresses.length > 0) {
      // Por defecto seleccionamos la primera (o se podria buscar isDefault si existe)
      selectedAddressIndex.value = 0
    }
  } catch (err) {
    console.error('Error fetching addresses:', err)
  } finally {
    loadingAddresses.value = false
  }
})

const currentPlan = computed(() => {
  if (!subStore.userSubscription) return null
  return subStore.userSubscription.plan || subStore.plans.find(p => p.id === subStore.userSubscription.plan?.id)
})

const adjustedTotals = computed(() => {
  const base = {
    subtotal: cartStore.cartSubtotal,
    shipping: cartStore.cartShippingCost,
    total: cartStore.cartTotal
  }

  if (!subStore.userSubscription) return base
  return subStore.applyBenefits(base)
})

const placeOrder = async () => {
  if (userAddresses.value.length === 0) {
    error.value = 'Necesitas añadir una dirección en tu perfil antes de continuar.'
    return
  }

  const chosenAddress = userAddresses.value[selectedAddressIndex.value]

  if (!chosenAddress || !chosenAddress.street || !chosenAddress.city || !chosenAddress.zipCode || !chosenAddress.country) {
    error.value = 'Dirección seleccionada inválida.'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const orderPayload = {
      items: cartStore.items.map(item => ({
        id: item.id,
        name: item.name,
        price: item.price,
        quantity: item.quantity,
        size: item.size
      })),
      shippingAddress: {
        street: chosenAddress.street,
        city: chosenAddress.city,
        state: chosenAddress.state || '',
        zipCode: chosenAddress.zipCode,
        country: chosenAddress.country
      },
      subtotal: adjustedTotals.value.subtotal,
      shippingCost: adjustedTotals.value.shipping,
      discount: cartStore.cartSubtotal - adjustedTotals.value.subtotal,
      total: adjustedTotals.value.total
    }

    await http.post('/orders/checkout', orderPayload)
    cartStore.clearCart() // Llama a limpiar carrito en front y persistido en local storage (opcional en auth) si lo manejas desde store
    router.push({ name: 'home', query: { orderSuccess: 'true' } })
  } catch (err) {
    error.value = 'Ocurrió un error al procesar el pedido. Inténtalo de nuevo.'
    console.error(err)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="checkout-container">
    <HeaderItem class="checkout-header" />

    <main class="checkout-main">
      <h1>Finalizar Pedido</h1>

      <div v-if="error" class="error-msg">{{ error }}</div>

      <div class="checkout-content">
        <div class="shipping-section">
          <h2>Dirección de Envío</h2>

          <div v-if="loadingAddresses" class="loading-state">
            Cargando direcciones...
          </div>

          <div v-else-if="userAddresses.length === 0" class="no-address-state">
            <p>No tienes ninguna dirección registrada.</p>
            <router-link to="/profile" class="btn-profile">Ir a mi Perfil para añadir una dirección</router-link>
          </div>

          <div v-else class="address-selector">
            <p>Selecciona la dirección donde deseas recibir el pedido:</p>
            <div class="addresses-list">
              <label
                v-for="(addr, index) in userAddresses"
                :key="index"
                class="address-card"
                :class="{ selected: selectedAddressIndex === index }"
              >
                <input
                  type="radio"
                  name="shippingAddress"
                  :value="index"
                  v-model="selectedAddressIndex"
                />
                <div class="address-info">
                  <strong>{{ addr.street }}</strong>
                  <span>{{ addr.city }}, {{ addr.state }} {{ addr.zipCode }}</span>
                  <span>{{ addr.country }}</span>
                </div>
              </label>
            </div>
          </div>
        </div>

        <div class="order-summary">
          <h2>Resumen de artículos</h2>
          <div class="summary-items">
            <div v-for="item in cartStore.items" :key="item.id + item.size" class="summary-item">
              <img :src="item.imageUrl" :alt="item.name" />
              <div class="item-details">
                <p class="item-name">{{ item.name }}</p>
                <p class="item-meta">Cant: {{ item.quantity }} <span v-if="item.size">| Talla: {{ item.size }}</span></p>
              </div>
              <div class="item-price">€{{ (item.price * item.quantity).toFixed(2) }}</div>
            </div>
          </div>

          <div class="totals">
            <div class="total-row">
              <span>Subtotal:</span>
              <span>
                <span v-if="currentPlan && currentPlan.discountPercentage > 0" class="original-price">
                  €{{ cartStore.cartSubtotal.toFixed(2) }}
                </span>
                €{{ adjustedTotals.subtotal.toFixed(2) }}
              </span>
            </div>
            <div v-if="currentPlan && currentPlan.discountPercentage > 0" class="total-row discount-info">
              <span>Descuento ({{ currentPlan.discountPercentage }}%):</span>
              <span class="discount-amount">-€{{ (cartStore.cartSubtotal - adjustedTotals.subtotal).toFixed(2) }}</span>
            </div>
            <div class="total-row">
              <span>Envío:</span>
              <span>€{{ adjustedTotals.shipping.toFixed(2) }}</span>
            </div>
            <div class="total-row grand-total">
              <span>Total a pagar:</span>
              <span>€{{ adjustedTotals.total.toFixed(2) }}</span>
            </div>
          </div>

          <button @click="placeOrder" :disabled="loading" class="btn-checkout">
            {{ loading ? 'Procesando...' : 'Finalizar Pedido' }}
          </button>
        </div>
      </div>
    </main>

    <FooterItem class="checkout-footer" />
  </div>
</template>

<style scoped>
.checkout-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.checkout-main {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.checkout-main h1 {
  text-align: center;
  color: #fff;
  font-size: 2.5rem;
  margin-bottom: 2rem;
}

.error-msg {
  background: #f8d7da;
  color: #721c24;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  text-align: center;
}

.checkout-content {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 2rem;
}

.shipping-section {
  background: #fff;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  height: fit-content;
}

.shipping-section h2 {
  margin-top: 0;
  margin-bottom: 1.5rem;
  color: #333;
}

.loading-state {
  color: #666;
  font-style: italic;
  padding: 2rem 0;
  text-align: center;
}

.no-address-state {
  text-align: center;
  padding: 2rem;
  background: #f9f9f9;
  border-radius: 8px;
  border: 1px dashed #ccc;
}

.no-address-state p {
  margin-bottom: 1.5rem;
  color: #555;
}

.btn-profile {
  display: inline-block;
  background: #667eea;
  color: white;
  padding: 0.8rem 1.5rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: bold;
  transition: background 0.3s;
}

.btn-profile:hover {
  background: #5568d3;
}

.address-selector p {
  margin-bottom: 1rem;
  color: #555;
}

.addresses-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.address-card {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.5rem;
  border: 2px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-card:hover {
  border-color: #ccc;
}

.address-card.selected {
  border-color: #667eea;
  background-color: #f8faff;
}

.address-card input[type="radio"] {
  margin-top: 0.2rem;
  width: 18px;
  height: 18px;
  accent-color: #667eea;
}

.address-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  color: #555;
}

.address-info strong {
  color: #333;
  font-size: 1.1rem;
}

.order-summary {
  background: #fff;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  position: sticky;
  top: 2rem;
  height: fit-content;
}

.order-summary h2 {
  margin-top: 0;
  margin-bottom: 1.5rem;
  color: #333;
}

.summary-items {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
  max-height: 300px;
  overflow-y: auto;
  padding-right: 0.5rem;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  border-bottom: 1px solid #eee;
  padding-bottom: 1rem;
}

.summary-item img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
}

.item-details {
  flex: 1;
}

.item-name {
  margin: 0 0 0.25rem 0;
  font-weight: 600;
  color: #333;
}

.item-meta {
  margin: 0;
  font-size: 0.85rem;
  color: #666;
}

.item-price {
  font-weight: 600;
  color: #667eea;
}

.totals {
  border-top: 2px solid #eee;
  padding-top: 1.5rem;
  margin-bottom: 1.5rem;
}

.total-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.75rem;
  color: #555;
}

.grand-total {
  font-size: 1.25rem;
  font-weight: bold;
  color: #333;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}

.btn-checkout {
  width: 100%;
  background: #28a745;
  color: white;
  padding: 1rem;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-checkout:hover {
  background: #218838;
}

.btn-checkout:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.original-price {
  text-decoration: line-through;
  color: #999;
  font-size: 0.9em;
  margin-right: 0.5rem;
}

.discount-info {
  color: #28a745;
}

@media (max-width: 768px) {
  .checkout-content {
    grid-template-columns: 1fr;
  }
}
</style>
