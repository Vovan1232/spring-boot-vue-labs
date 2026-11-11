<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <a class="skip-link" href="#main-content">Перейти до вмісту</a>
  <header class="site-header">
    <div class="container shell header-inner">
      <RouterLink class="brand" to="/" aria-label="Читальня — головна">
        <span class="brand-mark" aria-hidden="true">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round">
            <path d="M12 6.5C9.3 4.6 6.3 4.2 3.5 5v13c2.8-.8 5.8-.4 8.5 1.5 2.7-1.9 5.7-2.3 8.5-1.5V5c-2.8-.8-5.8-.4-8.5 1.5Z"/><path d="M12 6.5v13"/>
          </svg>
        </span>
        Читальня
      </RouterLink>
      <nav class="site-nav" aria-label="Основна навігація">
        <RouterLink to="/" exact-active-class="selected">Головна</RouterLink>
        <RouterLink to="/articles" :class="{ selected: route.path.startsWith('/articles') }">Каталог</RouterLink>
        <RouterLink to="/about" exact-active-class="selected">Про проєкт</RouterLink>
      </nav>
      <span class="header-tag">Лабораторні 03 / 04</span>
    </div>
  </header>
</template>
