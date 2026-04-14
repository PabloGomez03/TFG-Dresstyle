<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useCartStore } from '@/stores/cart';
import { getSizesForCategory } from '@/utils/categorySizes';
import FooterItem from '@/components/FooterItem.vue';
import HeaderItem from '@/components/HeaderItem.vue';
import http from '@/api/http';

const route = useRoute();
const cartStore = useCartStore();

const products = ref([]);
const loading = ref(false);
const selectedSizes = ref({});
const message = ref('');
const messageType = ref('');

const isCatalogMode = computed(() => route.name === 'catalog' || route.query.catalog === '1');

const visibleProducts = computed(() => {
  const searchQuery = route.query.q || '';

  if (searchQuery.trim()) {
    const query = searchQuery.toLowerCase();
    return products.value.filter(product =>
      product.name.toLowerCase().includes(query) ||
      product.description?.toLowerCase().includes(query)
    );
  }

  if (isCatalogMode.value) {
    return products.value.slice(0, 12);
  }

  return products.value;
});

const sectionTitle = computed(() => {
  if (isCatalogMode.value) {
    return 'Catálogo de Productos';
  }

  return 'Resultados de Búsqueda';
});

const emptyMessage = computed(() => {
  if (isCatalogMode.value) {
    return 'No hay productos disponibles en este momento.';
  }

  return 'No se encontraron productos que coincidan con tu búsqueda.';
});

const loadProducts = async () => {
  try {
    loading.value = true;
    const response = await http.get('/catalog/products');
    products.value = Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error('Error cargando productos:', error);
    products.value = [];
    showMessage('Error al cargar los productos', 'error');
  } finally {
    loading.value = false;
  }
};

const showMessage = (text, type) => {
  message.value = text;
  messageType.value = type;
  setTimeout(() => {
    message.value = '';
  }, 3000);
};

const addToCart = (product) => {
  const selectedSize = selectedSizes.value[product.id];
  const availableSizes = getSizesForCategory(product.category);

  // Si la categoría tiene más de una talla disponible y no es "Única", requiere selección
  if (availableSizes.length > 1 && availableSizes[0] !== 'Única' && !selectedSize) {
    showMessage('Por favor selecciona una talla', 'error');
    return;
  }

  cartStore.addItem(product, 1, selectedSize || null);
  showMessage(`${product.name} añadido al carrito`, 'success');
};

onMounted(() => {
  cartStore.loadCartFromStorage();
  loadProducts();
});
</script>

<template>
  <div class="search-container">
    <HeaderItem class="search-header" />

    <main class="search-main">
      <div class="search-section">
        <h1>{{ sectionTitle }}</h1>

        <div v-if="message" :class="['message', messageType]">
          {{ message }}
        </div>

        <div v-if="loading" class="loading">
          Cargando productos...
        </div>

        <div v-else-if="visibleProducts.length === 0" class="no-results">
          <p>{{ emptyMessage }}</p>
        </div>

        <div v-else class="products-grid">
          <div
            v-for="product in visibleProducts"
            :key="product.id"
            class="product-card"
          >
            <div class="product-image">
              <img :src="product.imageUrl" :alt="product.name" />
            </div>

            <div class="product-info">
              <h3>{{ product.name }}</h3>
              <p class="product-description">{{ product.description }}</p>

              <div class="product-price">
                <span class="price">${{ parseFloat(product.price).toFixed(2) }}</span>
              </div>

              <div class="size-selector">
                <label for="size">Talla:</label>
                <select
                  :id="`size-${product.id}`"
                  v-model="selectedSizes[product.id]"
                  class="size-select"
                >
                  <option value="">Selecciona una talla</option>
                  <option v-for="size in getSizesForCategory(product.category)" :key="size" :value="size">
                    {{ size }}
                  </option>
                </select>
              </div>

              <button
                class="btn-add-cart"
                :disabled="product.stock === 0"
                @click="addToCart(product)"
              >
                Añadir al carrito
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <FooterItem class="search-footer" />
  </div>
</template>

<style scoped>
.search-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: transparent;
}

.search-header {
  flex-shrink: 0;
}

.search-main {
  flex: 1;
  padding: 2rem;
  background: transparent;
}

.search-section {
  max-width: 1200px;
  margin: 0 auto;
}

.search-section h1 {
  text-align: center;
  font-size: 2.5rem;
  color: #ffffff;
  margin-bottom: 2rem;
  font-weight: 600;
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
  color: #666;
}

.no-results {
  text-align: center;
  padding: 3rem;
  background: rgba(255, 255, 255, 0.72);
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(2px);
}

.no-results p {
  font-size: 1.1rem;
  color: #666;
  margin: 0;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 2rem;
}

.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.product-image {
  width: 100%;
  height: 250px;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.75rem;
  box-sizing: border-box;
}

.product-image img {
  max-width: 100%;
  max-height: 100%;
  width: auto;
  height: auto;
  object-fit: contain;
  display: block;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-info {
  padding: 1.5rem;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-info h3 {
  font-size: 1.2rem;
  color: #333;
  margin: 0 0 0.5rem 0;
  font-weight: 600;
}

.product-description {
  font-size: 0.9rem;
  color: #666;
  margin: 0 0 1rem 0;
  min-height: 2.6em;
}

.product-price {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.price {
  font-size: 1.5rem;
  font-weight: 700;
  color: #667eea;
}

.size-selector {
  margin-bottom: 1rem;
}

.size-selector label {
  display: block;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #333;
  font-size: 0.95rem;
}

.size-select {
  width: 100%;
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
  padding: 0.8rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  margin-top: auto;
}

.btn-add-cart:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-add-cart:disabled {
  background: #ccc;
  cursor: not-allowed;
  opacity: 0.6;
}

.search-footer {
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .search-main {
    padding: 1rem;
  }

  .search-section h1 {
    font-size: 1.8rem;
    margin-bottom: 1.5rem;
  }

  .search-box {
    flex-direction: column;
  }

  .search-input {
    max-width: none;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 1rem;
  }

  .product-image {
    height: 200px;
  }

  .product-info {
    padding: 1rem;
  }
}
</style>
