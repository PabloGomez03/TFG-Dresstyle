<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import FooterItem from '@/components/FooterItem.vue';
import HeaderItem from '@/components/HeaderItem.vue';
import http from '@/api/http';

const authStore = useAuthStore();
const isEditing = ref(false);
const loading = ref(false);
const message = ref('');
const messageType = ref('');
const selectedAddressIndex = ref(0);
const maxAddresses = 5;

const emptyAddress = () => ({
  street: '',
  city: '',
  zipCode: '',
  country: ''
});

const profileData = reactive({
  userId: '',
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  addresses: []
});

const editFormData = reactive({
  firstName: '',
  lastName: '',
  phone: '',
  addresses: [emptyAddress()]
});

const selectedAddress = computed(() => {
  if (!profileData.addresses.length) return null;
  return profileData.addresses[selectedAddressIndex.value] || profileData.addresses[0];
});

const loadProfile = async () => {
  try {
    loading.value = true;
    const response = await http.get('/auth/profile');
    Object.assign(profileData, response.data);

    const loadedAddresses = Array.isArray(response.data.addresses) ? response.data.addresses : [];
    profileData.addresses = loadedAddresses;
    selectedAddressIndex.value = 0;

    Object.assign(editFormData, {
      firstName: response.data.firstName,
      lastName: response.data.lastName,
      phone: response.data.phone,
      addresses: loadedAddresses.length ? loadedAddresses.map((address) => ({ ...address })) : [emptyAddress()]
    });
  } catch (error) {
    console.error('Error cargando perfil:', error);
    showMessage('Error al cargar el perfil', 'error');
  } finally {
    loading.value = false;
  }
};

const toggleEdit = () => {
  if (isEditing.value) {
    Object.assign(editFormData, {
      firstName: profileData.firstName,
      lastName: profileData.lastName,
      phone: profileData.phone,
      addresses: profileData.addresses.length
        ? profileData.addresses.map((address) => ({ ...address }))
        : [emptyAddress()]
    });
  }
  isEditing.value = !isEditing.value;
};

const addAddress = () => {
  if (editFormData.addresses.length >= maxAddresses) {
    showMessage('Maximo 5 direcciones por usuario', 'error');
    return;
  }

  editFormData.addresses.push(emptyAddress());
};

const removeAddress = (index) => {
  if (editFormData.addresses.length === 1) {
    editFormData.addresses[0] = emptyAddress();
    return;
  }

  editFormData.addresses.splice(index, 1);
};

const saveProfile = async () => {
  try {
    loading.value = true;
    const payload = {
      firstName: editFormData.firstName,
      lastName: editFormData.lastName,
      phone: editFormData.phone,
      addresses: editFormData.addresses
        .map((address) => ({
          street: (address.street || '').trim(),
          city: (address.city || '').trim(),
          zipCode: (address.zipCode || '').trim(),
          country: (address.country || '').trim()
        }))
        .filter((address) => address.street || address.city || address.zipCode || address.country)
    };

    const response = await http.put('/auth/profile', payload);
    Object.assign(profileData, response.data);
    profileData.addresses = Array.isArray(response.data.addresses) ? response.data.addresses : [];
    selectedAddressIndex.value = 0;
    isEditing.value = false;
    showMessage('Perfil actualizado correctamente', 'success');
  } catch (error) {
    console.error('Error actualizando perfil:', error);
    showMessage(error.response?.data?.message || 'Error al actualizar el perfil', 'error');
  } finally {
    loading.value = false;
  }
};

const showMessage = (text, type) => {
  message.value = text;
  messageType.value = type;
  setTimeout(() => {
    message.value = '';
  }, 5000);
};

onMounted(() => {
  authStore.initializeAuth();
  loadProfile();
});
</script>


<template>

  <div class="profile-container">
    <HeaderItem class="profile-header" />

    <main class="profile-main">
      <div :class="['profile-card', { 'profile-card-editing': isEditing }]">

        <!-- Mensaje de éxito/error -->
        <div v-if="message" :class="['message', messageType]">
          {{ message }}
        </div>

        <!-- Modo Visualización -->
        <div v-if="!isEditing" class="profile-view">
          <div class="profile-header-title">
            <h1>Mi Perfil</h1>
            <button @click="toggleEdit" class="btn-edit">Editar Perfil</button>
          </div>

          <div class="profile-info">
            <div class="info-group">
              <label>Nombre:</label>
              <p>{{ profileData.firstName || 'No especificado' }}</p>
            </div>

            <div class="info-group">
              <label>Apellido:</label>
              <p>{{ profileData.lastName || 'No especificado' }}</p>
            </div>

            <div class="info-group">
              <label>Email:</label>
              <p>{{ profileData.email }}</p>
            </div>

            <div class="info-group">
              <label>Teléfono:</label>
              <p>{{ profileData.phone || 'No especificado' }}</p>
            </div>

            <div class="info-group full-width">
              <label>Direcciones:</label>

              <div v-if="profileData.addresses.length" class="address-selector">
                <select v-model.number="selectedAddressIndex" class="address-select">
                  <option v-for="(address, index) in profileData.addresses" :key="index" :value="index">
                    Direccion {{ index + 1 }}
                  </option>
                </select>

                <div class="address-details">
                  <p><strong>Calle:</strong> {{ selectedAddress?.street || 'No especificada' }}</p>
                  <p><strong>Ciudad:</strong> {{ selectedAddress?.city || 'No especificada' }}</p>
                  <p><strong>Código postal:</strong> {{ selectedAddress?.zipCode || 'No especificado' }}</p>
                  <p><strong>País:</strong> {{ selectedAddress?.country || 'No especificado' }}</p>
                </div>
              </div>

              <p v-else>No hay direcciones registradas</p>
            </div>
          </div>
        </div>

        <!-- Modo Edición -->
        <div v-else class="profile-edit">
          <div class="profile-header-title">
            <h1>Editar Perfil</h1>
          </div>

          <form @submit.prevent="saveProfile" class="edit-form">
            <div class="form-group">
              <label for="firstName">Nombre:</label>
              <input
                id="firstName"
                v-model="editFormData.firstName"
                type="text"
                placeholder="Ingresa tu nombre"
                required
              />
            </div>

            <div class="form-group">
              <label for="lastName">Apellido:</label>
              <input
                id="lastName"
                v-model="editFormData.lastName"
                type="text"
                placeholder="Ingresa tu apellido"
              />
            </div>

            <div class="form-group">
              <label for="email">Email (no editable):</label>
              <input
                id="email"
                :value="profileData.email"
                type="email"
                disabled
              />
            </div>

            <div class="form-group">
              <label for="phone">Teléfono:</label>
              <input
                id="phone"
                v-model="editFormData.phone"
                type="tel"
                placeholder="Ingresa tu teléfono"
              />
            </div>

            <div class="addresses-edit-section">
              <div class="addresses-edit-header">
                <h3>Direcciones</h3>
                <button
                  type="button"
                  class="btn-add-address"
                  @click="addAddress"
                  :disabled="editFormData.addresses.length >= maxAddresses"
                >
                  + Añadir dirección
                </button>
              </div>

              <p class="addresses-helper">Puedes guardar hasta {{ maxAddresses }} direcciones.</p>

              <div
                v-for="(address, index) in editFormData.addresses"
                :key="index"
                class="address-item"
              >
                <div class="address-item-header">
                  <span>Dirección {{ index + 1 }}</span>
                  <button
                    type="button"
                    class="btn-remove-address"
                    @click="removeAddress(index)"
                  >
                    Eliminar
                  </button>
                </div>

                <div class="form-group">
                  <label :for="`street-${index}`">Calle:</label>
                  <input
                    :id="`street-${index}`"
                    v-model="address.street"
                    type="text"
                    placeholder="Ingresa la calle"
                  />
                </div>

                <div class="form-group">
                  <label :for="`city-${index}`">Ciudad:</label>
                  <input
                    :id="`city-${index}`"
                    v-model="address.city"
                    type="text"
                    placeholder="Ingresa la ciudad"
                  />
                </div>

                <div class="form-group">
                  <label :for="`zipCode-${index}`">Código Postal:</label>
                  <input
                    :id="`zipCode-${index}`"
                    v-model="address.zipCode"
                    type="text"
                    placeholder="Ingresa el código postal"
                  />
                </div>

                <div class="form-group">
                  <label :for="`country-${index}`">País:</label>
                  <input
                    :id="`country-${index}`"
                    v-model="address.country"
                    type="text"
                    placeholder="Ingresa el país"
                  />
                </div>
              </div>
            </div>

            <div class="form-actions">
              <button
                type="submit"
                class="btn-save"
                :disabled="loading"
              >
                {{ loading ? 'Guardando...' : 'Guardar Cambios' }}
              </button>
              <button
                type="button"
                class="btn-cancel"
                @click="toggleEdit"
              >
                Cancelar
              </button>
            </div>
          </form>
        </div>
      </div>
    </main>

    <FooterItem class="profile-footer" />
  </div>

</template>

<style scoped>
.profile-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.profile-main {
  flex: 1;
  padding: 2rem;
  background: transparent;
}

.profile-card {
  max-width: 600px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(2px);
  padding: 2rem;
}

.profile-card-editing {
  max-width: 1000px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: none;
  padding: 0;
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 0;
}

.profile-header-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #e0e0e0;
}

.profile-header-title h1 {
  font-size: 1.8rem;
  color: #333;
  margin: 0;
}

.message {
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1.5rem;
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

/* Botones */
.btn-edit,
.btn-cancel,
.btn-save {
  padding: 0.6rem 1.2rem;
  border: none;
  border-radius: 4px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-edit {
  background-color: #007bff;
  color: white;
}

.btn-edit:hover {
  background-color: #0056b3;
  transform: translateY(-2px);
}

.btn-cancel {
  background-color: #6c757d;
  color: white;
}

.btn-cancel:hover {
  background-color: #5a6268;
}

.btn-save {
  background-color: #28a745;
  color: white;
  width: 100%;
  padding: 0.8rem;
  font-size: 1rem;
}

.btn-save:hover:not(:disabled) {
  background-color: #218838;
}

.btn-save:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* Vista de Perfil */
.profile-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.info-group {
  display: flex;
  flex-direction: column;
}

.info-group.full-width {
  grid-column: 1 / -1;
}

.info-group label {
  font-weight: 600;
  color: #666;
  margin-bottom: 0.4rem;
  font-size: 0.9rem;
  text-transform: uppercase;
}

.info-group p {
  color: #333;
  font-size: 1rem;
  margin: 0;
  word-break: break-word;
}

.address-selector {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.address-select {
  max-width: 220px;
  padding: 0.5rem 0.65rem;
  border: 1px solid #d7d7d7;
  border-radius: 6px;
}

.address-details {
  background: #f7f7f7;
  border-radius: 6px;
  padding: 0.75rem;
  display: grid;
  gap: 0.35rem;
}

.address-details p {
  margin: 0;
}

/* Formulario de Edición */
.edit-form {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  grid-column: 1;
}

.form-group:nth-child(3),
.form-group:nth-child(5),
.form-group:nth-child(6),
.form-group:nth-child(7) {
  grid-column: 1 / -1;
}

.form-group label {
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
  font-size: 0.95rem;
}

.form-group input {
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.95rem;
  transition: border-color 0.3s ease;
}

.form-group input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.25);
}

.form-group input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.form-actions {
  grid-column: 1 / -1;
  margin-top: 1rem;
}

.profile-edit {
  display: contents;
}

.profile-card-editing .profile-header-title {
  justify-content: center;
  border-bottom: none;
  margin-bottom: 2rem;
  padding-bottom: 0;
  grid-column: 1 / -1;
  padding: 2.5rem 2.5rem 0 2.5rem;
}

.profile-card-editing .profile-header-title h1 {
  text-align: center;
  font-size: 1.8rem;
  font-weight: 600;
}

.profile-card-editing .edit-form {
  display: block;
  padding: 0 2.5rem 2.5rem 2.5rem;
  grid-column: 1;
  grid-row: 2;
}

.profile-card-editing .form-group {
  max-width: none;
  margin-left: 0;
  margin-right: 0;
  margin-bottom: 1.5rem;
}

.profile-card-editing .form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.profile-card-editing .form-group input {
  width: 100%;
  display: block;
  box-sizing: border-box;
  padding: 0.75rem 1rem;
  border: 1.5px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
}

.profile-card-editing .form-actions {
  width: 100%;
  box-sizing: border-box;
  max-width: none;
  margin-left: 0;
  margin-right: 0;
  grid-column: 1;
  display: grid;
  gap: 0.75rem;
}

.profile-card-editing .addresses-edit-section {
  width: 100%;
  box-sizing: border-box;
  max-width: none;
  margin: 0;
  padding: 2.5rem 1.5rem;
  grid-column: 2;
  grid-row: 1 / -1;
  background: #f9f9f9;
  border-left: 1px solid #e0e0e0;
  border-radius: 0 12px 12px 0;
  overflow-y: auto;
  max-height: 600px;
}

.addresses-edit-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}

.addresses-edit-header h3 {
  margin: 0;
  font-size: 1rem;
  color: #333;
}

.addresses-helper {
  margin: 0.4rem 0 0.8rem;
  color: #6b6b6b;
  font-size: 0.85rem;
}

.address-item {
  border: 1px solid #ececec;
  border-radius: 8px;
  padding: 0.8rem;
  margin-bottom: 0.8rem;
  background: #fafafa;
}

.address-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.7rem;
}

.address-item-header span {
  font-size: 0.9rem;
  font-weight: 600;
}

.btn-add-address,
.btn-remove-address {
  border: 1px solid #d0d0d0;
  background: white;
  border-radius: 6px;
  padding: 0.35rem 0.6rem;
  cursor: pointer;
}

.btn-add-address:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.profile-card-editing .form-group input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  background-color: #f9f9ff;
}

.profile-card-editing .btn-save {
  width: 100%;
  margin-top: 0;
  padding: 0.875rem;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  order: 1;
}

.profile-card-editing .btn-save:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.profile-card-editing .btn-cancel {
  width: 100%;
  margin-top: 0;
  border: 1.5px solid #ddd;
  border-radius: 8px;
  background: white;
  color: #555;
  order: 2;
}

.profile-card-editing .btn-cancel:hover {
  background: #f6f6f6;
  color: #333;
}

@media (max-width: 600px) {
  .profile-card {
    padding: 1.5rem;
  }

  .profile-card-editing {
    padding: 1.5rem;
    grid-template-columns: 1fr;
  }

  .profile-info,
  .edit-form {
    grid-template-columns: 1fr;
  }

  .profile-header-title {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
    padding: 1.5rem 1.5rem 0 1.5rem;
  }

  .btn-edit,
  .btn-cancel {
    width: 100%;
  }

  .profile-card-editing .addresses-edit-section {
    grid-column: 1;
    grid-row: auto;
    border-left: none;
    border-radius: 0;
    border-top: 1px solid #e0e0e0;
    max-height: none;
    padding: 1.5rem;
  }

  .profile-card-editing .edit-form {
    padding: 0 1.5rem 1.5rem 1.5rem;
  }
}

</style>
