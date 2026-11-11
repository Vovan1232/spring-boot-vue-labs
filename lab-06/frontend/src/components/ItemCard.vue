<script setup>
defineProps({
  article: { type: Object, required: true },
})
defineEmits(['remove'])
</script>

<template>
  <article class="card article-card h-100">
    <div class="card-body d-flex flex-column">
      <p class="eyebrow article-number">Запис №{{ String(article.id).padStart(2, '0') }}</p>
      <h2 class="article-title">{{ article.title }}</h2>
      <p class="article-author">{{ article.author }}</p>
      <p v-if="article.publicationYear" class="small text-secondary">
        {{ article.publicationYear }} · {{ article.category }} · {{ article.language }}
      </p>
      <p class="article-summary">{{ article.summary }}</p>
      <div class="article-actions mt-auto">
        <RouterLink class="btn btn-outline-secondary btn-sm" :to="{ name: 'edit', params: { id: article.id } }">Редагувати</RouterLink>
        <button class="btn btn-delete btn-sm" type="button" @click="$emit('remove', article)">Видалити</button>
      </div>
    </div>
  </article>
</template>
