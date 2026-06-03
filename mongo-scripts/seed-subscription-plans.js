
db = db.getSiblingDB('dresstyle_subscription');


db.subscriptionPlans.deleteMany({});


db.subscriptionPlans.insertMany([
  {
    name: "Basic",
    description: "Plan básico con envío gratis",
    price: 0,
    benefits: {
      freeShipping: true,
      discountPct: 0
    },
    isActive: true,
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    name: "Premium",
    description: "Plan premium con envío gratis y 5% descuento",
    price: 9.99,
    benefits: {
      freeShipping: true,
      discountPct: 5
    },
    isActive: true,
    createdAt: new Date(),
    updatedAt: new Date()
  }
]);

print("Subscription plans seeded successfully!");
