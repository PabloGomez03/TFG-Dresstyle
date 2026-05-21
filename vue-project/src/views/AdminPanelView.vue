<script setup>
import HeaderItem from '@/components/HeaderItem.vue'
import http from '@/api/http'
import { computed, onMounted, ref } from 'vue'
import { getCategoriesList } from '@/utils/categorySizes'

const CLOUDINARY_CLOUD_NAME = (import.meta.env.VITE_CLOUDINARY_CLOUD_NAME || '').trim()
const CLOUDINARY_UPLOAD_PRESET = (import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET || '').trim()

const products = ref([])
const editingId = ref(null)
const isModalOpen = ref(false)
const selectedImageFile = ref(null)
const isUploadingImage = ref(false)
const isLoadingProducts = ref(false)
const isSavingProduct = ref(false)
const isDeleteModalOpen = ref(false)
const productToDelete = ref(null)
const message = ref('')
const errorMessage = ref('')
const searchQuery = ref('')
const currentPage = ref(0)
const itemsPerPage = 12
const totalItems = ref(0)
const totalPages = ref(0)
const isSearching = ref(false)
let searchDebounceTimer = null

const form = ref({
  name: '',
  description: '',
  imageUrl: '',
  price: '',
  stock: '',
  category: ''
})

const isEditing = computed(() => editingId.value !== null)


onMounted(() => {
  fetchProducts()
})

function getErrorMessage(error, fallbackMessage) {
  return error?.response?.data?.message || error?.message || fallbackMessage
}

async function fetchProducts() {
  clearFeedback()
  isLoadingProducts.value = true
  isSearching.value = true

  try {
    const params = {
      page: currentPage.value,
      size: itemsPerPage
    }

    if (searchQuery.value.trim()) {
      params.q = searchQuery.value.trim()
    }

    const response = await http.get('/catalog/products', { params })
    const { content, totalElements, totalPages: pages } = response.data

    products.value = Array.isArray(content) ? content : []
    totalItems.value = totalElements || 0
    totalPages.value = pages || 0
  } catch (error) {

    if (error?.response?.status === 401) {
      window.location.href = '/ds/auth/login?redirect=/admin'
      return
    }

    products.value = []
    totalItems.value = 0
    totalPages.value = 0
    errorMessage.value = getErrorMessage(error, 'No se pudieron cargar los productos.')
  } finally {
    isLoadingProducts.value = false
    isSearching.value = false
  }
}

function resetForm() {
  form.value = {
    name: '',
    description: '',
    imageUrl: '',
    price: '',
    stock: '',
    category: ''
  }
  selectedImageFile.value = null
  isUploadingImage.value = false
  editingId.value = null
}

function openCreateModal() {
  clearFeedback()
  resetForm()
  isModalOpen.value = true
}

function openEditModal(product) {
  clearFeedback()
  editingId.value = product.id
  form.value = {
    name: product.name || '',
    description: product.description || '',
    imageUrl: product.imageUrl || '',
    price: String(product.price ?? ''),
    stock: String(product.stock ?? ''),
    category: product.category || ''
  }
  isModalOpen.value = true
}

function closeModal() {
  isModalOpen.value = false
  resetForm()
}

function clearFeedback() {
  message.value = ''
  errorMessage.value = ''
}

function validateForm() {
  if (!form.value.name.trim()) {
    return 'El nombre del producto es obligatorio.'
  }

  if (!form.value.category.trim()) {
    return 'La categoría del producto es obligatoria.'
  }

  if (!form.value.imageUrl.trim()) {
    return 'Sube una imagen a Cloudinary antes de guardar el producto.'
  }

  const parsedPrice = Number(form.value.price)
  if (Number.isNaN(parsedPrice) || parsedPrice <= 0) {
    return 'El precio debe ser un número mayor que 0.'
  }

  const parsedStock = Number(form.value.stock)
  if (!Number.isInteger(parsedStock) || parsedStock < 0) {
    return 'El stock debe ser un número entero igual o mayor que 0.'
  }

  return null
}

function onImageSelected(event) {
  const selectedFile = event.target.files?.[0] || null
  selectedImageFile.value = selectedFile

  if (selectedFile) {
    uploadImageToCloudinary()
  }
}

async function uploadImageToCloudinary() {
  clearFeedback()

  if (!CLOUDINARY_CLOUD_NAME || !CLOUDINARY_UPLOAD_PRESET) {
    errorMessage.value = 'Configura VITE_CLOUDINARY_CLOUD_NAME y VITE_CLOUDINARY_UPLOAD_PRESET en el .env.'
    return
  }

  if (!selectedImageFile.value) {
    errorMessage.value = 'Selecciona una imagen antes de subirla.'
    return
  }

  isUploadingImage.value = true

  try {
    const payload = new FormData()
    payload.append('file', selectedImageFile.value)
    payload.append('upload_preset', CLOUDINARY_UPLOAD_PRESET)

    const response = await fetch(`https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`, {
      method: 'POST',
      body: payload
    })

    if (!response.ok) {
      let cloudinaryErrorMessage = 'No se pudo subir la imagen a Cloudinary.'

      try {
        const errorPayload = await response.json()
        cloudinaryErrorMessage = errorPayload?.error?.message || errorPayload?.message || cloudinaryErrorMessage
      } catch {
        // Si Cloudinary no devuelve JSON, mantenemos el mensaje por defecto.
      }

      throw new Error(cloudinaryErrorMessage)
    }

    const data = await response.json()
    form.value.imageUrl = data.secure_url || data.url || ''
    selectedImageFile.value = null
    message.value = 'Imagen subida correctamente.'
  } catch (error) {
    errorMessage.value = error.message || 'Error al subir imagen a Cloudinary.'
  } finally {
    isUploadingImage.value = false
  }
}

async function saveProduct() {
  clearFeedback()
  const validationError = validateForm()

  if (validationError) {
    errorMessage.value = validationError
    return
  }

  if (isUploadingImage.value) {
    errorMessage.value = 'Espera a que termine la subida de la imagen.'
    return
  }

  isSavingProduct.value = true

  const productData = {
    name: (form.value.name || '').trim(),
    description: (form.value.description || '').trim(),
    imageUrl: (form.value.imageUrl || '').trim(),
    price: Number(form.value.price),
    stock: Number(form.value.stock),
    category: (form.value.category || '').trim()
  }

  try {
    if (isEditing.value) {
      const response = await http.put(`/catalog/products/${editingId.value}`, productData)
      const updatedProduct = response.data

      products.value = products.value.map((product) => {
        if (product.id !== editingId.value) return product
        return updatedProduct
      })
      message.value = 'Producto actualizado correctamente.'
    } else {
      const response = await http.post('/catalog/products', productData)
      const createdProduct = response.data
      products.value.unshift(createdProduct)
      message.value = 'Producto añadido correctamente.'
    }

    closeModal()
  } catch (error) {
    errorMessage.value = getErrorMessage(error, 'No se pudo guardar el producto.')
  } finally {
    isSavingProduct.value = false
  }
}

async function deleteProduct(productId) {
  clearFeedback()

  try {
    await http.delete(`/catalog/products/${productId}`)
    products.value = products.value.filter((product) => product.id !== productId)

    if (editingId.value === productId) {
      closeModal()
    }

    message.value = 'Producto eliminado correctamente.'
  } catch (error) {
    errorMessage.value = getErrorMessage(error, 'No se pudo eliminar el producto.')
  }
}

function confirmDeleteProduct(product) {
  productToDelete.value = product
  isDeleteModalOpen.value = true
}

function closeDeleteModal() {
  isDeleteModalOpen.value = false
  productToDelete.value = null
}

function executeDeleteProduct() {
  if (!productToDelete.value?.id) {
    closeDeleteModal()
    return
  }

  const productId = productToDelete.value.id
  closeDeleteModal()
  deleteProduct(productId)
}

function cancelEdit() {
  closeModal()
}

function onSearchInput() {
  currentPage.value = 0

  // Clear any existing debounce timer
  if (searchDebounceTimer) {
    clearTimeout(searchDebounceTimer)
  }

  // Set a new debounce timer to avoid too many requests while typing
  searchDebounceTimer = setTimeout(() => {
    fetchProducts()
    searchDebounceTimer = null
  }, 500) // Wait 500ms after user stops typing before searching
}


function goToNextPage() {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++
    fetchProducts()
  }
}

function goToPreviousPage() {
  if (currentPage.value > 0) {
    currentPage.value--
    fetchProducts()
  }
}
</script>

<template>
  <div class="admin-view">
    <HeaderItem class="main-header" />

    <main class="content">
      <h1>Panel de Administrador</h1>

      <section class="card">
        <div class="section-header">
          <h2>Productos</h2>
          <button type="button" @click="openCreateModal">Añadir producto</button>
        </div>

        <div v-if="message" class="feedback success">{{ message }}</div>
        <div v-if="errorMessage" class="feedback error">{{ errorMessage }}</div>

        <div class="search-bar">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Buscar por nombre, descripción o categoría..."
            @input="onSearchInput"
            class="search-input"
          />
          <span v-if="searchQuery" class="search-results">
            {{ isSearching ? 'Buscando...' : totalItems + ' resultado' + (totalItems !== 1 ? 's' : '') }}
          </span>
        </div>

        <p v-if="isLoadingProducts || isSearching" class="empty-state">Cargando productos...</p>

        <p v-else-if="products.length === 0" class="empty-state">
          {{ totalItems === 0 ? 'No hay productos en el catálogo.' : 'No se encontraron productos que coincidan con tu búsqueda.' }}
        </p>

        <div v-else class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Imagen</th>
                <th>Nombre</th>
                <th>Descripción</th>
                <th>Categoría</th>
                <th>Precio</th>
                <th>Stock</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="product in products" :key="product.id">
                <td>
                  <img v-if="product.imageUrl" :src="product.imageUrl" alt="Imagen producto" class="product-thumb" />
                  <span v-else>-</span>
                </td>
                <td>{{ product.name }}</td>
                <td class="product-description">{{ product.description || '-' }}</td>
                <td>{{ product.category || '-' }}</td>
                <td>{{ product.price.toFixed(2) }} €</td>
                <td>{{ product.stock }}</td>
                <td class="action-buttons">
                  <button type="button" class="secondary" @click="openEditModal(product)">Modificar</button>
                  <button type="button" class="danger" @click="confirmDeleteProduct(product)">Eliminar</button>
                </td>
              </tr>
            </tbody>
          </table>

          <div class="pagination">
            <button
              type="button"
              class="pagination-btn"
              @click="goToPreviousPage"
              :disabled="currentPage === 0 || isSearching"
            >
              ← Anterior
            </button>

            <div class="pagination-info">
              <span>Página {{ currentPage + 1 }} de {{ totalPages }}</span>
              <span class="items-count">{{ totalItems }} producto{{ totalItems !== 1 ? 's' : '' }}</span>
            </div>

            <button
              type="button"
              class="pagination-btn"
              @click="goToNextPage"
              :disabled="currentPage === totalPages - 1 || isSearching || totalPages === 0"
            >
              Siguiente →
            </button>
          </div>
        </div>
      </section>

      <div v-if="isModalOpen" class="modal-overlay" @click.self="cancelEdit">
        <div class="modal">
          <h2>{{ isEditing ? 'Modificar producto' : 'Añadir producto' }}</h2>

          <form class="admin-form" @submit.prevent="saveProduct">
            <div class="form-group">
              <label for="name">Nombre</label>
              <input id="name" v-model="form.name" type="text" required />
            </div>

            <div class="form-group">
              <label for="description">Descripción</label>
              <textarea id="description" v-model="form.description" rows="3" />
            </div>

            <div class="form-group">
              <label for="imageFile">Subir imagen</label>
              <div class="upload-row">
                <input id="imageFile" type="file" accept="image/*" @change="onImageSelected" />
                <span class="upload-status" :class="{ loading: isUploadingImage }">
                  {{ isUploadingImage ? 'Subiendo...' : 'La imagen se sube automaticamente al seleccionarla' }}
                </span>
              </div>
            </div>

            <div v-if="form.imageUrl" class="preview-wrapper">
              <img :src="form.imageUrl" alt="Preview" class="preview-image" />
            </div>

            <div class="form-group">
              <label for="category">Categoría</label>
              <select id="category" v-model="form.category" required>
                <option value="">Selecciona una categoría</option>
                <option v-for="cat in getCategoriesList()" :key="cat" :value="cat">
                  {{ cat }}
                </option>
              </select>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="price">Precio</label>
                <input id="price" v-model="form.price" type="number" min="0.01" step="0.01" required />
              </div>

              <div class="form-group">
                <label for="stock">Stock</label>
                <input id="stock" v-model="form.stock" type="number" min="0" step="1" required />
              </div>
            </div>

            <div v-if="errorMessage" class="feedback error">{{ errorMessage }}</div>

            <div class="actions">
              <button type="submit" :disabled="isSavingProduct || isUploadingImage">
                {{ isSavingProduct ? 'Guardando...' : isEditing ? 'Guardar cambios' : 'Añadir producto' }}
              </button>
              <button type="button" class="secondary" @click="cancelEdit">Cancelar</button>
            </div>
          </form>
        </div>
      </div>

      <div v-if="isDeleteModalOpen" class="modal-overlay delete-overlay" @click.self="closeDeleteModal">
        <div class="modal delete-modal" role="dialog" aria-modal="true" aria-labelledby="delete-modal-title">
          <div class="delete-badge" aria-hidden="true">!</div>
          <h2 id="delete-modal-title">Eliminación de producto</h2>
          <p class="delete-message">
            ¿Seguro que quieres eliminar <strong>{{ (productToDelete && productToDelete.name) || 'este producto' }}</strong>?
          </p>
          <p class="delete-warning">Esta acción no se puede deshacer.</p>

          <div class="actions delete-actions">
            <button type="button" class="danger" @click="executeDeleteProduct">Confirmar</button>
            <button type="button" class="secondary" @click="closeDeleteModal">Cancelar</button>
          </div>
        </div>
      </div>
    </main>



  </div>
</template>

<style scoped>
.admin-view {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-footer {
  margin-top: auto;
}

.content {
  flex: 1;
  max-width: 1100px;
  margin: 0 auto;
  width: 100%;
  padding: 2rem;
  display: grid;
  gap: 1.5rem;
}

.content h1 {
  color: #ffffff;
  margin-bottom: 0.5rem;
}

.content p {
  color: #ffffff;
}

.card {
  background-color: #ffffff;
  border-radius: 10px;
  padding: 1.5rem;
}

.card h2 {
  margin-top: 0;
  margin-bottom: 0;
  color: #2b2b2b;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  gap: 1rem;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 250px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 0.6rem 0.75rem;
  font-family: inherit;
  font-size: 0.95rem;
  box-sizing: border-box;
}

.search-input:focus {
  outline: none;
  border-color: #2f80ed;
  box-shadow: 0 0 0 3px rgba(47, 128, 237, 0.1);
}

.search-results {
  color: #6b7280;
  font-size: 0.9rem;
  white-space: nowrap;
}

.admin-form {
  display: grid;
  gap: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.form-group {
  display: grid;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 600;
  color: #333333;
}

.form-group input,
.form-group textarea {
  width: 100%;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 0.6rem 0.75rem;
  font-family: inherit;
  box-sizing: border-box;
}

.upload-row {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  flex-wrap: wrap;
}

.upload-status {
  color: #4b5563;
  font-size: 0.9rem;
}

.upload-status.loading {
  color: #1d4ed8;
  font-weight: 600;
}

.preview-wrapper {
  margin-top: -0.25rem;
}

.preview-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #d1d5db;
}

.actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

button {
  border: none;
  border-radius: 8px;
  padding: 0.55rem 0.9rem;
  background-color: #2f80ed;
  color: #ffffff;
  cursor: pointer;
}

button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

button.secondary {
  background-color: #6b7280;
}

button.danger {
  background-color: #dc2626;
}

.feedback {
  border-radius: 8px;
  padding: 0.65rem 0.75rem;
  font-size: 0.92rem;
  margin-bottom: 1rem;
}

.feedback.success {
  background-color: #ecfdf3;
  color: #166534;
}

.feedback.error {
  background-color: #fef2f2;
  color: #991b1b;
}

.empty-state {
  margin: 0;
  color: #525252;
}

.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

th,
td {
  padding: 0.7rem;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
  vertical-align: middle;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

th {
  color: #374151;
  font-weight: 600;
}

th:nth-child(1) {
  width: 80px;
}

th:nth-child(2) {
  width: 150px;
}

th:nth-child(3) {
  width: 25%;
}

th:nth-child(4) {
  width: 100px;
}

th:nth-child(5) {
  width: 90px;
}

th:nth-child(6) {
  width: 70px;
}

th:nth-child(7) {
  width: 150px;
}

td:nth-child(3) {
  white-space: normal;
  overflow: visible;
  text-overflow: initial;
}

td:nth-child(7) {
  white-space: normal;
  overflow: visible;
  text-overflow: initial;
}

.product-thumb {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  object-fit: cover;
  border: 1px solid #e5e7eb;
}

.product-description {
  white-space: normal;
  word-wrap: break-word;
  max-height: 3em;
  overflow: hidden;
  line-height: 1em;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
  flex-wrap: wrap;
}

.pagination-btn {
  padding: 0.55rem 1rem;
  background-color: #2f80ed;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  background-color: #1e6bc8;
}

.pagination-btn:disabled {
  background-color: #d1d5db;
  color: #9ca3af;
  cursor: not-allowed;
}

.pagination-info {
  display: flex;
  gap: 1rem;
  color: #6b7280;
  font-size: 0.9rem;
  align-items: center;
  white-space: nowrap;
}

.items-count {
  color: #2f80ed;
  font-weight: 600;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(17, 24, 39, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  z-index: 1000;
}

.modal {
  width: 100%;
  max-width: 640px;
  background-color: #ffffff;
  border-radius: 10px;
  padding: 1.5rem;
}

.delete-overlay {
  backdrop-filter: blur(4px);
}

.delete-modal {
  max-width: 460px;
  text-align: center;
  border: 1px solid rgba(220, 38, 38, 0.12);
  box-shadow: 0 24px 60px rgba(17, 24, 39, 0.24);
}

/* Force readable text colors inside the delete modal (overrides .content p white) */
.delete-modal {
  color: #111827; /* base text color */
}

.delete-modal p {
  color: #374151;
}

.delete-modal .delete-warning {
  color: #b91c1c;
}

.delete-badge {
  width: 52px;
  height: 52px;
  margin: 0 auto 0.9rem;
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: #fee2e2;
  color: #b91c1c;
  font-size: 1.5rem;
  font-weight: 800;
}

.modal h2 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #2b2b2b;
}

.delete-message {
  margin: 0;
  color: #374151;
  font-size: 1rem;
}

.delete-warning {
  margin: 0.75rem 0 0;
  color: #b91c1c;
  font-size: 0.92rem;
  font-weight: 600;
}

.delete-actions {
  justify-content: center;
  margin-top: 1.5rem;
}

.delete-actions .danger {
  background-color: #dc2626;
}

.delete-actions .secondary {
  background-color: #6b7280;
}

@media (max-width: 780px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .search-input {
    min-width: 100%;
  }

  .section-header {
    flex-direction: column;
    align-items: stretch;
  }

  .section-header button {
    width: 100%;
  }

  .pagination {
    flex-direction: column;
    gap: 0.75rem;
  }

  .pagination-btn {
    width: 100%;
  }

  .pagination-info {
    flex-direction: column;
    gap: 0.5rem;
    text-align: center;
  }

  .content {
    padding: 1rem;
  }
}
</style>
