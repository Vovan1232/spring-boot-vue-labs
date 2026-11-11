<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import ItemCard from '../components/ItemCard.vue'
import { apiError, articlesApi } from '../api.js'
import { notice } from '../feedback.js'

const articles = ref([])
const search = ref('')
const loading = ref(false)
const loadError = ref('')
const deleteError = ref('')
const pending = ref(null)
const removing = ref(false)
const deleteDialog = ref(null)
const searchInput = ref(null)
const success = ref(notice.value)
notice.value = ''

const filtered = computed(() => {
  const query = search.value.trim().toLocaleLowerCase('uk')
  return articles.value.filter((article) =>
    [article.title, article.author, article.summary].some((field) =>
      field.toLocaleLowerCase('uk').includes(query)
    )
  )
})

async function load() {
  loading.value = true
  loadError.value = ''
  await articlesApi.list()
    .then((result) => { articles.value = result })
    .catch((error) => { loadError.value = apiError(error) })
    .finally(() => { loading.value = false })
}

async function confirmRemoval(article) {
  pending.value = article
  deleteError.value = ''
  await nextTick()
  deleteDialog.value.showModal()
}

async function remove() {
  if (removing.value) return
  removing.value = true
  deleteError.value = ''
  const id = pending.value.id
  await articlesApi.remove(id)
    .then(() => {
      articles.value = articles.value.filter((article) => article.id !== id)
      success.value = 'Статтю видалено.'
      deleteDialog.value.close()
      searchInput.value.focus()
    })
    .catch((error) => { deleteError.value = apiError(error) })
    .finally(() => { removing.value = false })
}

onMounted(load)
</script>

<template>
  <div class="container shell page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">Читальня / Колекція</p>
        <h1>Каталог статей</h1>
        <p class="page-lead">Переглядайте, додавайте й оновлюйте записи в колекції.</p>
      </div>
      <RouterLink class="btn btn-primary" to="/articles/new"><span aria-hidden="true">＋</span> Додати статтю</RouterLink>
    </div>

    <div v-if="success" class="alert alert-success d-flex justify-content-between gap-3 align-items-center" role="status">
      {{ success }}
      <button class="btn-close" type="button" aria-label="Закрити повідомлення" @click="success = ''"></button>
    </div>
    <div v-if="loading" class="status-panel" role="status">Завантажуємо статті…</div>
    <div v-else-if="loadError" class="status-panel">
      <p class="mb-3" role="alert">{{ loadError }}</p>
      <button class="btn btn-outline-secondary" type="button" @click="load">Повторити запит</button>
    </div>
    <template v-else>
      <div class="catalog-tools">
        <div class="search-box">
          <label class="visually-hidden" for="search">Пошук статей</label>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" aria-hidden="true"><circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 4.5 4.5"/></svg>
          <input id="search" ref="searchInput" v-model="search" class="form-control" type="search" placeholder="Назва, автор або опис">
        </div>
        <span class="record-count">Показано {{ filtered.length }} з {{ articles.length }}</span>
      </div>
      <div v-if="filtered.length" class="row g-3">
        <div v-for="article in filtered" :key="article.id" class="col-12 col-md-6 col-lg-4">
          <ItemCard :article="article" @remove="confirmRemoval" />
        </div>
      </div>
      <div v-else class="status-panel">
        {{ articles.length ? 'За цим запитом статей не знайдено.' : 'Каталог порожній. Додайте першу статтю.' }}
      </div>
    </template>

    <dialog ref="deleteDialog" class="delete-dialog" aria-labelledby="delete-title"
      @cancel="removing && $event.preventDefault()" @close="pending = null">
      <div v-if="pending">
        <p class="eyebrow">Підтвердження дії</p>
        <h2 id="delete-title">Видалити статтю?</h2>
        <p>Запис «{{ pending.title }}» буде видалено з каталогу.</p>
        <p v-if="deleteError" class="alert alert-danger" role="alert">{{ deleteError }}</p>
        <div class="d-flex flex-wrap justify-content-end gap-2">
          <button class="btn btn-outline-secondary" type="button" autofocus :disabled="removing" @click="deleteDialog.close()">Скасувати</button>
          <button class="btn btn-danger" type="button" :disabled="removing" @click="remove">
            {{ removing ? 'Видаляємо…' : 'Так, видалити' }}
          </button>
        </div>
      </div>
    </dialog>
  </div>
</template>
