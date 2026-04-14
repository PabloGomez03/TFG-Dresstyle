<script setup>
import { useRouter } from 'vue-router'
import HeaderItem from '@/components/HeaderItem.vue'
import FooterItem from '@/components/FooterItem.vue'

const router = useRouter()

const openCatalog = () => {
  const { href } = router.resolve({ name: 'catalog' })
  window.open(href, '_blank', 'noopener')
}
</script>

<template>

  <div class="home-view">

  <HeaderItem class="main-header"/>

  <div class="content">
    <div class="hero">
      <h1>Bienvenido a DressStyle</h1>
      <p>Descubre tu estilo único con nuestra colección exclusiva de moda</p>
    </div>

    <div class="features">
      <div class="feature-card">
        <div class="feature-icon">👗</div>
        <h3>Colecciones Exclusivas</h3>
        <p>Diseños únicos y tendencias actuales</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon">✨</div>
        <h3>Calidad Premium</h3>
        <p>Prendas de alta calidad y durabilidad</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon">🚚</div>
        <h3>Envío Rápido</h3>
        <p>Entregas rápidas a toda la región</p>
      </div>
    </div>

    <div class="cta-section">
      <h2>Explora Nuestras Colecciones</h2>
      <button class="cta-button" @click="openCatalog">Ver Catálogo</button>
    </div>

  </div>

  <FooterItem  class="main-footer"/>

  </div>

</template>


<style scoped>

.home-view {
  display: flex;
  flex-direction: column; /*Ocupa toda la altura de la ventana de home*/
  min-height: 100vh;
}

.main-footer {
  /* Empuja el componente al final del contenedor */
  margin-top: auto;
}


.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3rem;
  padding: 3rem 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.hero {
  text-align: center;
  padding: 2rem 0;
}

.hero h1 {
  font-size: 3rem;
  color: #ffffff;
  margin-bottom: 1rem;
  font-weight: 700;
}

.hero p {
  font-size: 1.2rem;
  color: #ffffff;
  max-width: 600px;
  margin: 0 auto;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 2rem;
}

.feature-card {
  background: linear-gradient(135deg, #f5f5f5 0%, #ffffff 100%);
  padding: 2rem;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 12px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature-card h3 {
  color: #333;
  font-size: 1.3rem;
  margin-bottom: 0.5rem;
}

.feature-card p {
  color: #888;
  font-size: 0.95rem;
}

.cta-section {
  text-align: center;
  padding: 2rem;
  background: linear-gradient(135deg, #5261a7 0%, #232caa 100%);
  border-radius: 12px;
  color: white;
}

.cta-section h2 {
  font-size: 2rem;
  margin-bottom: 1.5rem;
}

.cta-button {
  background-color: white;
  color: #667eea;
  padding: 1rem 2.5rem;
  font-size: 1.1rem;
  font-weight: 600;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cta-button:hover {
  background-color: #f0f0f0;
  transform: scale(1.05);
}

@media (max-width: 768px) {
  .hero h1 {
    font-size: 2rem;
  }

  .hero p {
    font-size: 1rem;
  }

  .content {
    padding: 2rem 1rem;
    gap: 2rem;
  }

  .cta-section h2 {
    font-size: 1.5rem;
  }
}

</style>
