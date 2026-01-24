<script setup></script>
<template>
<footer class="footer">
    <div class="footer-content">
      <div class="footer-section">
        <h3>DresStyle</h3>
        <p>Tu plataforma de moda exclusiva con muchisimos descuentos.</p>
      </div>
      <div class="footer-section">
        <h4>Enlaces</h4>
        <ul>
          <li><router-link to="/contacto">Contacto</router-link></li>
          <li><router-link to="/terminos">Términos de Servicio</router-link></li>
        </ul>
      </div>
    </div>
    <div class="footer-bottom">
      &copy; 2026 DresStyle - Trabajo de Fin de Grado
    </div>
  </footer>

</template>
<style scoped>
.footer {
  background-color: #f0eded;
  padding: 2rem 0 1rem 0;
  margin-top: auto;
  border-top: 1px solid #ddd;
}
.footer-content {
  display: flex;
  justify-content: space-around;
  max-width: 1200px;
  margin: 0 auto;
}
.footer-section h3, .footer-section h4 {
  margin-bottom: 1rem;
  color: #333;
}
.footer-section ul { list-style: none; padding: 0; }
.footer-section li { margin-bottom: 0.5rem; }
.footer-bottom {
  text-align: center;
  margin-top: 2rem;
  font-size: 0.8rem;
  color: #777;
}
</style>
