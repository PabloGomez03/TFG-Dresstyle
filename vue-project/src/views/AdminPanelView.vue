<script setup>
import HeaderItem from '@/components/HeaderItem.vue'
import FooterItem from '@/components/FooterItem.vue'
import { computed, ref } from 'vue'

const STORAGE_KEY = 'admin_products'

const products = ref(loadProducts())
const editingId = ref(null)
const isModalOpen = ref(false)
const message = ref('')
const errorMessage = ref('')

const form = ref({
  name: '',
  description: '',
  price: '',
  stock: ''
})

const isEditing = computed(() => editingId.value !== null)

function loadProducts() {
  const rawProducts = localStorage.getItem(STORAGE_KEY)
  if (!rawProducts) return []

  try {
    const parsedProducts = JSON.parse(rawProducts)
    return Array.isArray(parsedProducts) ? parsedProducts : []
  } catch {
    return []
  }
}

function persistProducts() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(products.value))
}

function resetForm() {
  form.value = {
    name: '',
    description: '',
    price: '',
    stock: ''
  }
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
    name: product.name,
    description: product.description,
    price: String(product.price),
    stock: String(product.stock)
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

function saveProduct() {
  clearFeedback()
  const validationError = validateForm()

  if (validationError) {
    errorMessage.value = validationError
    return
  }

  const productData = {
    name: form.value.name.trim(),
    description: form.value.description.trim(),
    price: Number(form.value.price),
    stock: Number(form.value.stock)
  }

  if (isEditing.value) {
    products.value = products.value.map((product) => {
      if (product.id !== editingId.value) return product
      return { ...product, ...productData }
    })
    message.value = 'Producto actualizado correctamente.'
  } else {
    products.value.unshift({
      id: crypto.randomUUID(),
      ...productData
    })
    message.value = 'Producto añadido correctamente.'
  }

  persistProducts()
  closeModal()
}

function deleteProduct(productId) {
  clearFeedback()
  products.value = products.value.filter((product) => product.id !== productId)
  persistProducts()

  if (editingId.value === productId) {
    closeModal()
  }

  message.value = 'Producto eliminado correctamente.'
}

function cancelEdit() {
  closeModal()
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

        <p v-if="products.length === 0" class="empty-state">No hay productos en el catálogo.</p>

        <div v-else class="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Descripción</th>
                <th>Precio</th>
                <th>Stock</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="product in products" :key="product.id">
                <td>{{ product.name }}</td>
                <td>{{ product.description || '-' }}</td>
                <td>{{ product.price.toFixed(2) }} €</td>
                <td>{{ product.stock }}</td>
                <td class="action-buttons">
                  <button type="button" class="secondary" @click="openEditModal(product)">Modificar</button>
                  <button type="button" class="danger" @click="deleteProduct(product.id)">Eliminar</button>
                </td>
              </tr>
            </tbody>
          </table>
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
              <button type="submit">{{ isEditing ? 'Guardar cambios' : 'Añadir producto' }}</button>
              <button type="button" class="secondary" @click="cancelEdit">Cancelar</button>
            </div>
          </form>
        </div>
      </div>
    </main>

    <FooterItem class="main-footer" />

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
}

th,
td {
  padding: 0.7rem;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
}

th {
  color: #374151;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
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

.modal h2 {
  margin-top: 0;
  margin-bottom: 1rem;
  color: #2b2b2b;
}

@media (max-width: 780px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .content {
    padding: 1rem;
  }
}
</style>
