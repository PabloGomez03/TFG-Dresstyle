import { defineStore } from 'pinia';
import { useAuthStore } from '@/stores/auth';
import http from '@/api/http';

const CART_STORAGE_KEY = 'cart';

function getCartStorage() {
  return typeof window !== 'undefined' ? window.sessionStorage : null;
}

function getLegacyCartStorage() {
  return typeof window !== 'undefined' ? window.localStorage : null;
}

function saveCart(items) {
  const storage = getCartStorage();
  if (!storage) return;

  storage.setItem(CART_STORAGE_KEY, JSON.stringify(items));
}

function removeCart() {
  const storage = getCartStorage();
  if (storage) {
    storage.removeItem(CART_STORAGE_KEY);
  }
}

function readCart() {
  const sessionStorage = getCartStorage();
  const storedCart = sessionStorage?.getItem(CART_STORAGE_KEY);

  if (storedCart) {
    try {
      const parsed = JSON.parse(storedCart);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      sessionStorage?.removeItem(CART_STORAGE_KEY);
      return [];
    }
  }

  const legacyStorage = getLegacyCartStorage();
  const legacyCart = legacyStorage?.getItem(CART_STORAGE_KEY);

  if (legacyCart) {
    let parsedLegacy = [];
    try {
      const parsed = JSON.parse(legacyCart);
      parsedLegacy = Array.isArray(parsed) ? parsed : [];
    } catch {
      legacyStorage?.removeItem(CART_STORAGE_KEY);
      return [];
    }

    if (sessionStorage) {
      sessionStorage.setItem(CART_STORAGE_KEY, JSON.stringify(parsedLegacy));
    }

    legacyStorage.removeItem(CART_STORAGE_KEY);
    return parsedLegacy;
  }

  return [];
}

function isLoggedInUser() {
  const authStore = useAuthStore();
  return authStore.isAuthenticated && authStore.isTokenValid;
}

function normalizeItem(item) {
  return {
    id: item?.id ?? '',
    name: item?.name ?? '',
    price: Number(item?.price ?? 0),
    imageUrl: item?.imageUrl ?? '',
    quantity: Math.max(1, Number(item?.quantity ?? 1)),
    size: item?.size ?? null
  };
}

function mergeItems(baseItems, guestItems) {
  const merged = baseItems.map(normalizeItem);

  guestItems.map(normalizeItem).forEach((guestItem) => {
    const existingItem = merged.find(
      (item) => item.id === guestItem.id && item.size === guestItem.size
    );

    if (existingItem) {
      existingItem.quantity += guestItem.quantity;
      return;
    }

    merged.push(guestItem);
  });

  return merged;
}

async function loadRemoteCart() {
  const response = await http.get('/orders/cart');
  return Array.isArray(response.data?.items) ? response.data.items.map(normalizeItem) : [];
}

async function saveRemoteCart(items) {
  await http.put('/orders/cart', { items: items.map(normalizeItem) });
}

function readGuestCartOnly() {
  return readCart();
}

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
  }),

  getters: {
    cartItemsCount: (state) => state.items.length,
    cartTotal: (state) => {
      return state.items.reduce((total, item) => total + (item.price * item.quantity), 0);
    }
  },

  actions: {
    async addItem(product, quantity = 1, size = null) {
      // Buscar si el producto con la misma talla ya está en el carrito
      const existingItem = this.items.find(
        item => item.id === product.id && item.size === size
      );

      if (existingItem) {
        existingItem.quantity += quantity;
      } else {
        this.items.push({
          id: product.id,
          name: product.name,
          price: product.price,
          imageUrl: product.imageUrl,
          quantity,
          size
        });
      }

      await this.persistCart();
    },

    async removeItem(itemIndex) {
      this.items.splice(itemIndex, 1);
      await this.persistCart();
    },

    async updateQuantity(itemIndex, quantity) {
      if (quantity <= 0) {
        await this.removeItem(itemIndex);
      } else {
        this.items[itemIndex].quantity = quantity;
        await this.persistCart();
      }
    },

    async clearCart() {
      this.items = [];
      if (isLoggedInUser()) {
        await http.delete('/orders/cart');
        return;
      }

      removeCart();
    },

    async persistCart() {
      if (isLoggedInUser()) {
        try {
          await saveRemoteCart(this.items);
        } catch (error) {
          console.error('Error guardando el carrito remoto:', error);
        }
        return;
      }

      saveCart(this.items);
    },

    async loadCartFromStorage() {
      if (isLoggedInUser()) {
        const guestItems = readGuestCartOnly();

        try {
          const remoteItems = await loadRemoteCart();
          this.items = guestItems.length ? mergeItems(remoteItems, guestItems) : remoteItems;

          if (guestItems.length) {
            await saveRemoteCart(this.items);
            removeCart();
          }
          return;
        } catch (error) {
          console.error('Error cargando el carrito remoto:', error);
        }
      }

      this.items = readGuestCartOnly();
    }
  }
});
