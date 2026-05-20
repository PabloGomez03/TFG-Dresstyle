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
        const res = await http.get('/subscription/my-subscription')
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
        await http.post('/subscription/cancel')
        this.userSubscription = null
      } finally {
        this.loading = false
      }
    },
    applyBenefits(cart) {
      if (!this.userSubscription) return cart
      const plan = this.userSubscription.plan || this.plans.find(p => p.id === this.userSubscription.plan?.id)
      if (!plan) return cart

      const discountPct = plan?.discountPercentage || 0
      const freeShipping = !!plan?.freeShipping

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
