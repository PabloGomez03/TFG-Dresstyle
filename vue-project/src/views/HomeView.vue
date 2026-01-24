<script setup>
import HeaderItem from '@/components/HeaderItem.vue'
import FooterItem from '@/components/FooterItem.vue'
</script>

<template>

  <div class="home-view">

  <HeaderItem class="main-header"/>

  <div class="content"></div>

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
}

</style>
