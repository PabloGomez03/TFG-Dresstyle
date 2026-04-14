<script setup>
import { onMounted, ref } from 'vue';
import { useCartStore } from '@/stores/cart';
import { useAuthStore } from '@/stores/auth';
import FooterItem from '@/components/FooterItem.vue';
import HeaderItem from '@/components/HeaderItem.vue';

const cartStore = useCartStore();
const authStore = useAuthStore();
const message = ref('');
const messageType = ref('');

const showMessage = (text, type) => {
  message.value = text;
  messageType.value = type;

  setTimeout(() => {
    message.value = '';
  }, 3500);
};

const goHome = () => {
  authStore.initializeAuth();
  if (!(authStore.isAuthenticated && authStore.isTokenValid)) {
    showMessage('Debes registrarte o iniciar sesion para continuar con la compra.', 'error');
    return;
  }

  window.location.assign('/');
};

onMounted(() => {
  authStore.initializeAuth();
  cartStore.loadCartFromStorage();
});
</script>

<template>
  <div class="cart-container">
    <HeaderItem class="cart-header" />

    <main class="cart-main">
      <div class="cart-section">
        <h1>Mi Carrito de Compras</h1>

        <div v-if="message" :class="['message', messageType]">
          {{ message }}
        </div>

        <div v-if="cartStore.items.length === 0" class="empty-cart">
          <p>Tu carrito está vacío. Busca productos para agregarlos.</p>
          <button @click="goHome" class="btn-continue-shopping">Continuar Comprando</button>
        </div>

        <div v-else class="cart-content">
          <div class="cart-items">
            <div v-for="(item, index) in cartStore.items" :key="index" class="cart-item">
              <div class="item-image">
                <img :src="item.imageUrl" :alt="item.name" />
              </div>

              <div class="item-info">
                <h3>{{ item.name }}</h3>
                <p v-if="item.size" class="item-size">Talla: {{ item.size }}</p>
                <p class="item-price">${{ parseFloat(item.price).toFixed(2) }}</p>
              </div>

              <div class="item-quantity">
                <button
                  @click="cartStore.updateQuantity(index, item.quantity - 1)"
                  class="btn-qty"
                >
                  -
                </button>
                <input
                  :value="item.quantity"
                  type="number"
                  min="1"
                  @input="cartStore.updateQuantity(index, parseInt($event.target.value) || 1)"
                  class="qty-input"
                />
                <button
                  @click="cartStore.updateQuantity(index, item.quantity + 1)"
                  class="btn-qty"
                >
                  +
                </button>
              </div>

              <div class="item-total">
                <p>${{ (item.price * item.quantity).toFixed(2) }}</p>
              </div>

              <button
                @click="cartStore.removeItem(index)"
                class="btn-remove"
              >
                🗑️
              </button>
            </div>
          </div>

          <div class="cart-summary">
            <h2>Resumen del Pedido</h2>
            <div class="summary-row">
              <span>Subtotal:</span>
              <span>${{ cartStore.cartTotal.toFixed(2) }}</span>
            </div>
            <div class="summary-row">
              <span>Envío:</span>
              <span>$0.00</span>
            </div>
            <div class="summary-row total">
              <span>Total:</span>
              <span>${{ cartStore.cartTotal.toFixed(2) }}</span>
            </div>
            <button @click="goHome" class="btn-continue-shopping">
              Continuar Comprando
            </button>
          </div>
        </div>
      </div>
    </main>

    <FooterItem class="cart-footer" />
  </div>
</template>

<style scoped>
.cart-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.cart-header {
  flex-shrink: 0;
}

.cart-main {
  flex: 1;
  padding: 2rem;
  background: transparent;
}

.cart-section {
  max-width: 1300px;
  margin: 0 auto;
}

.cart-section h1 {
  text-align: center;
  font-size: 2.5rem;
  color: #ffffff;
  margin-bottom: 2rem;
  font-weight: 600;
}

.message {
  max-width: 860px;
  margin: 0 auto 1.25rem;
  padding: 0.85rem 1rem;
  border-radius: 8px;
  font-weight: 600;
  text-align: center;
}

.message.error {
  background: rgba(248, 215, 218, 0.95);
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.empty-cart {
  text-align: center;
  background: white;
  padding: 3rem;
  max-width: 860px;
  margin: 0 auto;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.empty-cart p {
  font-size: 1.2rem;
  color: #666;
  margin: 0 0 1.5rem 0;
}

.cart-content {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 2rem;
}

.cart-items {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.cart-item {
  display: grid;
  grid-template-columns: 100px 1fr 120px 100px 50px;
  gap: 1rem;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 100px;
  height: 100px;
  overflow: hidden;
  border-radius: 8px;
  background: #f5f5f5;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info h3 {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 1rem;
}

.item-size {
  color: #666;
  font-size: 0.85rem;
  margin: 0;
}

.item-price {
  color: #667eea;
  font-weight: 600;
  margin: 0.5rem 0 0 0;
  font-size: 1.1rem;
}

.item-quantity {
  display: flex;
  align-items: center;
  border: 1px solid #ddd;
  border-radius: 6px;
  overflow: hidden;
}

.btn-qty {
  background: #f5f5f5;
  border: none;
  padding: 0.5rem 0.75rem;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.3s ease;
}

.btn-qty:hover {
  background: #e0e0e0;
}

.qty-input {
  width: 40px;
  text-align: center;
  border: none;
  padding: 0.5rem;
  font-weight: 600;
}

.qty-input::-webkit-inner-spin-button,
.qty-input::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.item-total {
  text-align: right;
  font-weight: 600;
  color: #333;
}

.item-total p {
  margin: 0;
  font-size: 1.1rem;
}

.btn-remove {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.btn-remove:hover {
  transform: scale(1.2);
}

.cart-summary {
  background: white;
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  height: fit-content;
  position: sticky;
  top: 2rem;
}

.cart-summary h2 {
  font-size: 1.3rem;
  margin: 0 0 1.5rem 0;
  color: #333;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 1rem;
  color: #666;
}

.summary-row.total {
  border-top: 2px solid #eee;
  padding-top: 1rem;
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
}

.btn-continue-shopping {
  width: 100%;
  padding: 1rem;
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  margin-top: 0.75rem;
  transition: all 0.3s ease;
}

.btn-continue-shopping:hover {
  background: #f5f5f5;
}

.cart-footer {
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .message {
    max-width: 100%;
  }

  .cart-content {
    grid-template-columns: 1fr;
  }

  .cart-item {
    grid-template-columns: 80px 1fr 40px;
    gap: 0.75rem;
    padding: 1rem;
  }

  .item-image {
    width: 80px;
    height: 80px;
  }

  .item-quantity,
  .item-total {
    display: none;
  }

  .cart-summary {
    position: static;
  }
}
</style>
