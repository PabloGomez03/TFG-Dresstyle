<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCartStore } from '@/stores/cart';
import { getSizesForCategory } from '@/utils/categorySizes';
import FooterItem from '@/components/FooterItem.vue';
import HeaderItem from '@/components/HeaderItem.vue';
import http from '@/api/http';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();

const products = ref([]);
const loading = ref(false);
const currentPage = ref(0);
const totalPages = ref(0);
const totalItems = ref(0);
const selectedSizes = ref({});
const message = ref('');
const messageType = ref('');

const isCatalogMode = computed(() => route.name === 'catalog' || route.query.catalog === '1');

const visibleProducts = computed(() => {
  // El backend ya devuelve la lista filtrada/paginada desde Mongo.
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

const extractProducts = (data) => {
  if (Array.isArray(data)) {
    return data;
  }

  if (Array.isArray(data?.content)) {
    return data.content;
  }

  return [];
};

const getPageNumbers = () => {
  if (totalPages.value <= 1) {
    return [];
  }

  const pages = [];
  const visibleRadius = 2;
  const start = Math.max(0, currentPage.value - visibleRadius);
  const end = Math.min(totalPages.value - 1, currentPage.value + visibleRadius);

  for (let page = start; page <= end; page += 1) {
    pages.push(page);
  }

  return pages;
};

const updateRoutePage = (page) => {
  const nextQuery = { ...route.query, page: String(page) };

  if (!nextQuery.q) {
    delete nextQuery.q;
  }

  if (route.name === 'catalog') {
    nextQuery.catalog = '1';
  }

  router.push({ name: route.name || 'search', query: nextQuery });
};

const goToPage = (page) => {
  if (page < 0 || page >= totalPages.value || page === currentPage.value) {
    return;
  }

  updateRoutePage(page);
};

const loadProducts = async () => {
  try {
    loading.value = true;
    const searchQuery = String(route.query.q || '').trim();
    const routePage = Number.parseInt(route.query.page ?? '0', 10);
    const params = {
      page: Number.isNaN(routePage) || routePage < 0 ? 0 : routePage,
      size: 12
    };

    if (searchQuery) {
      params.q = searchQuery;
    }

    const response = await http.get('/catalog/products', { params });
    products.value = extractProducts(response.data);
    currentPage.value = Number(response.data?.currentPage ?? params.page) || 0;
    totalPages.value = Number(response.data?.totalPages ?? 0) || 0;
    totalItems.value = Number(response.data?.totalElements ?? products.value.length) || 0;
  } catch (error) {
    console.error('Error cargando productos:', error);
    products.value = [];
    currentPage.value = 0;
    totalPages.value = 0;
    totalItems.value = 0;
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

const addToCart = async (product) => {
  const selectedSize = selectedSizes.value[product.id];
  const availableSizes = getSizesForCategory(product.category);

  // Si la categoría tiene más de una talla disponible y no es "Única", requiere selección
  if (availableSizes.length > 1 && availableSizes[0] !== 'Única' && !selectedSize) {
    showMessage('Por favor selecciona una talla', 'error');
    return;
  }

  try {
    await cartStore.addItem(product, 1, selectedSize || null);
    showMessage(`${product.name} añadido al carrito`, 'success');
  } catch (error) {
    console.error('Error añadiendo producto al carrito:', error);
    showMessage('No se pudo añadir el producto al carrito', 'error');
  }
};

onMounted(() => {
  cartStore.loadCartFromStorage();
  loadProducts();
});

watch(
  () => [route.name, route.query.q, route.query.catalog, route.query.page],
  () => {
    loadProducts();
  }
);
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
            <router-link :to="{ name: 'product-detail', params: { id: product.id } }" class="product-link">
              <div class="product-image">
                <img :src="product.imageUrl" :alt="product.name" />
              </div>

              <div class="product-info">
                <h3>{{ product.name }}</h3>
                <p class="product-description">{{ product.description }}</p>

                <div class="product-price">
                  <span class="price">€{{ parseFloat(product.price).toFixed(2) }}</span>
                </div>
              </div>
            </router-link>

            <div class="product-actions">
              <div class="size-selector">
                <label :for="`size-${product.id}`">Talla:</label>
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

        <div v-if="!loading && totalPages > 1" class="pagination">
          <button
            type="button"
            class="pagination-button"
            :disabled="currentPage === 0"
            @click="goToPage(currentPage - 1)"
          >
            Anterior
          </button>

          <button
            v-for="page in getPageNumbers()"
            :key="page"
            type="button"
            class="pagination-button"
            :class="{ active: page === currentPage }"
            @click="goToPage(page)"
          >
            {{ page + 1 }}
          </button>

          <button
            type="button"
            class="pagination-button"
            :disabled="currentPage >= totalPages - 1"
            @click="goToPage(currentPage + 1)"
          >
            Siguiente
          </button>
        </div>

        <p v-if="!loading && totalPages > 0" class="pagination-summary">
          Mostrando {{ products.length }} de {{ totalItems }} productos
        </p>
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

.pagination {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-top: 2rem;
}

.pagination-button {
  min-width: 3rem;
  padding: 0.75rem 1rem;
  border: 1px solid rgba(34, 93, 154, 0.25);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  color: #12304f;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease, background-color 0.2s ease, color 0.2s ease;
}

.pagination-button:hover:not(:disabled) {
  transform: translateY(-1px);
  background: #e6f0fb;
}

.pagination-button.active {
  background: #1d4ed8;
  color: #ffffff;
  border-color: #1d4ed8;
}

.pagination-button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.pagination-summary {
  margin: 1rem 0 0;
  text-align: center;
  color: #ffffff;
  font-weight: 500;
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

.product-link {
  flex: 1;
  display: flex;
  flex-direction: column;
  text-decoration: none;
  color: inherit;
  cursor: pointer;
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

.product-actions {
  padding: 1.5rem;
  border-top: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 1rem;
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

  .product-actions {
    padding: 1rem;
  }
}
</style>
