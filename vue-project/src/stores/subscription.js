import { defineStore } from 'pinia'
import http from '@/api/http'

export const useSubscriptionStore = defineStore('subscription', {
  state: () => ({
    plans: [],
    userSubscription: null,
    loading: false,
    userSubscriptionLoaded: false,
    userSubscriptionLoading: false,
    plansLoaded: false
  }),
  actions: {
    reset() {
      this.plans = []
      this.userSubscription = null
      this.loading = false
      this.userSubscriptionLoaded = false
      this.userSubscriptionLoading = false
      this.plansLoaded = false
    },
    async fetchPlans({ force = false } = {}) {
      if (this.plansLoaded && !force) return

      try {
        const res = await http.get('/subscription/plans')
        this.plans = Array.isArray(res.data) ? res.data : res.data?.content || []
        this.plansLoaded = true
      } catch {
        this.plans = []
      }
    },
    async fetchUserSubscription({ force = false } = {}) {
      if (this.userSubscriptionLoading) return
      if (this.userSubscriptionLoaded && !force) return

      this.userSubscriptionLoading = true

      try {
        const res = await http.get('/subscription/my-subscription')
        this.userSubscription = res.status === 204 || !res.data ? null : res.data
      } catch (error) {
        const status = error?.response?.status
        if (status === 404 || status === 204) {
          this.userSubscription = null
        }
      } finally {
        this.userSubscriptionLoaded = true
        this.userSubscriptionLoading = false
      }
    },
    async subscribe(planId, autoRenew = true) {
      this.loading = true
      try {
        const res = await http.post('/subscription/subscribe', { planId, autoRenew })
        this.userSubscription = res.data
        this.userSubscriptionLoaded = true
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
        this.userSubscriptionLoaded = true
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
