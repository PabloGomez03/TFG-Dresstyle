import { defineStore } from 'pinia'
import http from '@/api/http'

export const useSubscriptionStore = defineStore('subscription', {
  state: () => ({
    plans: [],
    userSubscription: null,
    loading: false
  }),
  actions: {
    async fetchPlans() {
      try {
        const res = await http.get('/subscription/plans')
        this.plans = Array.isArray(res.data) ? res.data : res.data?.content || []
      } catch  {
        this.plans = []
      }
    },
    async fetchUserSubscription() {
      try {
        const res = await http.get('/subscription/user')
        this.userSubscription = res.data
      } catch {
        this.userSubscription = null
      }
    },
    async subscribe(planId, autoRenew = true) {
      this.loading = true
      try {
        const res = await http.post('/subscription/subscribe', { planId, autoRenew })
        this.userSubscription = res.data
        return res.data
      } finally {
        this.loading = false
      }
    },
    async unsubscribe() {
      this.loading = true
      try {
        await http.delete('/subscription/unsubscribe')
        this.userSubscription = null
      } finally {
        this.loading = false
      }
    },
    // cart is an object: { subtotal, shipping, total } or items array
    applyBenefits(cart) {
      if (!this.userSubscription) return cart
      const plan = this.plans.find(p => p.id === this.userSubscription.planId)
      if (!plan) return cart

      const discountPct = plan?.benefits?.discountPct || 0
      const freeShipping = !!plan?.benefits?.freeShipping

      const subtotal = typeof cart.subtotal === 'number' ? cart.subtotal : cart.subtotal || 0
      const shipping = typeof cart.shipping === 'number' ? cart.shipping : cart.shipping || 0

      const discountedSubtotal = +(subtotal * (1 - discountPct / 100)).toFixed(2)
      const adjustedShipping = freeShipping ? 0 : shipping
      const total = +(discountedSubtotal + adjustedShipping).toFixed(2)

      return {
        ...cart,
        subtotal: discountedSubtotal,
        shipping: adjustedShipping,
        total
      }
    }
  }
})
