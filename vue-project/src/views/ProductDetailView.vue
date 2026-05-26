<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCartStore } from '@/stores/cart';
import { getSizesForCategory } from '@/utils/categorySizes';
import FooterItem from '@/components/FooterItem.vue';
import HeaderItem from '@/components/HeaderItem.vue';
import http from '@/api/http';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();

const product = ref(null);
const loading = ref(false);
const selectedSize = ref('');
const message = ref('');
const messageType = ref('');
const quantity = ref(1);

const productId = computed(() => route.params.id);

const availableSizes = computed(() => {
  if (!product.value) return [];
  return getSizesForCategory(product.value.category);
});

const showMessage = (text, type) => {
  message.value = text;
  messageType.value = type;
  setTimeout(() => {
    message.value = '';
  }, 3000);
};

const loadProduct = async () => {
  try {
    loading.value = true;
    const response = await http.get(`/catalog/products/${productId.value}`);
    product.value = response.data;
  } catch (error) {
    console.error('Error cargando producto:', error);
    showMessage('Error al cargar el producto', 'error');
    setTimeout(() => {
      router.push('/catalog');
    }, 2000);
  } finally {
    loading.value = false;
  }
};

const addToCart = async () => {
  if (!product.value) return;

  if (availableSizes.value.length > 1 && availableSizes.value[0] !== 'Única' && !selectedSize.value) {
    showMessage('Por favor selecciona una talla', 'error');
    return;
  }

  if (quantity.value < 1) {
    showMessage('La cantidad debe ser al menos 1', 'error');
    return;
  }

  try {
    await cartStore.addItem(product.value, quantity.value, selectedSize.value || null);
    showMessage(`${product.value.name} añadido al carrito`, 'success');
    setTimeout(() => {
      router.push('/cart');
    }, 1500);
  } catch (error) {
    console.error('Error añadiendo producto al carrito:', error);
    showMessage('No se pudo añadir el producto al carrito', 'error');
  }
};

const goBack = () => {
  router.back();
};

onMounted(() => {
  cartStore.loadCartFromStorage();
  loadProduct();
});
</script>

<template>
  <div class="detail-container">
    <HeaderItem class="detail-header" />

    <main class="detail-main">
      <div class="detail-section">
        <button class="btn-back" @click="goBack">
          ← Volver
        </button>

        <div v-if="message" :class="['message', messageType]">
          {{ message }}
        </div>

        <div v-if="loading" class="loading">
          Cargando producto...
        </div>

        <div v-else-if="!product" class="no-product">
          <p>Producto no encontrado</p>
        </div>

        <div v-else class="product-detail">
          <div class="product-image-section">
            <div class="product-image-wrapper">
              <img
                :src="product.imageUrl"
                :alt="product.name"
                class="product-image"
              />
            </div>
          </div>

          <div class="product-details-section">
            <h1 class="product-name">{{ product.name }}</h1>

            <div class="product-category">
              <span class="category-badge">{{ product.category }}</span>
            </div>

            <div class="product-description">
              <h2>Descripción</h2>
              <p>{{ product.description }}</p>
            </div>

            <div class="product-price-section">
              <span class="price">€{{ parseFloat(product.price).toFixed(2) }}</span>
              <span v-if="product.stock > 0" class="stock-available">
                Stock disponible: {{ product.stock }} unidades
              </span>
              <span v-else class="stock-unavailable">
                Producto agotado
              </span>
            </div>

            <div class="purchase-section">
              <div class="quantity-selector">
                <label for="quantity">Cantidad:</label>
                <div class="quantity-input">
                  <button @click="quantity = Math.max(1, quantity - 1)" class="qty-btn">-</button>
                  <input
                    v-model.number="quantity"
                    type="number"
                    min="1"
                    :max="product.stock"
                    class="qty-input"
                  />
                  <button @click="quantity = Math.min(product.stock, quantity + 1)" class="qty-btn">+</button>
                </div>
              </div>

              <div v-if="availableSizes.length > 0" class="size-selector">
                <label for="size">Talla:</label>
                <select v-model="selectedSize" class="size-select">
                  <option value="">Selecciona una talla</option>
                  <option v-for="size in availableSizes" :key="size" :value="size">
                    {{ size }}
                  </option>
                </select>
              </div>

              <button
                class="btn-add-cart"
                :disabled="product.stock === 0"
                @click="addToCart"
              >
                Añadir al carrito
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <FooterItem class="detail-footer" />
  </div>
</template>

<style scoped>
.detail-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: transparent;
}

.detail-header {
  flex-shrink: 0;
}

.detail-main {
  flex: 1;
  padding: 2rem;
  background: transparent;
}

.detail-section {
  max-width: 1000px;
  margin: 0 auto;
}

.btn-back {
  padding: 0.6rem 1.2rem;
  background: rgba(255, 255, 255, 0.9);
  color: #12304f;
  border: none;
  border-radius: 6px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  margin-bottom: 2rem;
  transition: all 0.3s ease;
}

.btn-back:hover {
  background: #ffffff;
  transform: translateX(-3px);
}

.message {
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  text-align: center;
  font-weight: 500;
}

.message.success {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.message.error {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.loading {
  text-align: center;
  padding: 3rem;
  font-size: 1.1rem;
  color: #ffffff;
}

.no-product {
  text-align: center;
  padding: 3rem;
  background: rgba(255, 255, 255, 0.72);
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.no-product p {
  font-size: 1.1rem;
  color: #666;
  margin: 0;
}

.product-detail {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.product-image-section {
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image-wrapper {
  width: 100%;
  max-width: 400px;
  aspect-ratio: 1;
  background: #f5f5f5;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  box-sizing: border-box;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.product-details-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.product-name {
  font-size: 2rem;
  color: #333;
  margin: 0;
  font-weight: 700;
}

.product-category {
  display: flex;
  gap: 0.5rem;
}

.category-badge {
  display: inline-block;
  background: #e6f0fb;
  color: #1d4ed8;
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
}

.product-description {
  padding: 1rem;
  background: #f9f9f9;
  border-radius: 8px;
  border-left: 4px solid #667eea;
}

.product-description h2 {
  font-size: 1rem;
  color: #333;
  margin: 0 0 0.5rem 0;
}

.product-description p {
  color: #666;
  margin: 0;
  line-height: 1.6;
}

.product-price-section {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.price {
  font-size: 2rem;
  font-weight: 700;
  color: #667eea;
}

.stock-available,
.stock-unavailable {
  font-size: 0.95rem;
  font-weight: 500;
}

.stock-available {
  color: #27ae60;
}

.stock-unavailable {
  color: #e74c3c;
}

.purchase-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.quantity-selector,
.size-selector {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.quantity-selector label,
.size-selector label {
  font-weight: 600;
  color: #333;
  font-size: 0.95rem;
}

.quantity-input {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  border: 1.5px solid #ddd;
  border-radius: 6px;
  padding: 0.5rem;
  background: white;
}

.qty-btn {
  background: #f5f5f5;
  border: none;
  padding: 0.4rem 0.8rem;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  color: #333;
  transition: all 0.2s ease;
}

.qty-btn:hover {
  background: #e6e6e6;
}

.qty-input {
  flex: 1;
  border: none;
  text-align: center;
  font-size: 1rem;
  font-weight: 600;
  outline: none;
}

.size-select {
  padding: 0.7rem;
  border: 1.5px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
  background: white;
  cursor: pointer;
  transition: border-color 0.3s ease;
}

.size-select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.btn-add-cart {
  padding: 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.05rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  margin-top: 1rem;
}

.btn-add-cart:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.btn-add-cart:disabled {
  background: #ccc;
  cursor: not-allowed;
  opacity: 0.6;
}

.detail-footer {
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .detail-main {
    padding: 1rem;
  }

  .product-detail {
    grid-template-columns: 1fr;
    gap: 2rem;
    padding: 1.5rem;
  }

  .product-name {
    font-size: 1.5rem;
  }

  .price {
    font-size: 1.5rem;
  }

  .product-image-wrapper {
    max-width: 100%;
  }
}
</style>
