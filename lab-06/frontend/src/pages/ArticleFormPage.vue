<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { apiError, articlesApi } from '../api.js'
import { notice } from '../feedback.js'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const loading = ref(Boolean(id))
const saving = ref(false)
const loadError = ref('')
const saveError = ref('')
const form = reactive({ title: '', author: '', summary: '' })

async function load() {
  loading.value = true
  loadError.value = ''
  await articlesApi.get(id)
    .then(({ title, author, summary }) => Object.assign(form, { title, author, summary }))
    .catch((error) => { loadError.value = apiError(error) })
    .finally(() => { loading.value = false })
}

async function save() {
  if (saving.value) return
  saveError.value = ''
  if (!form.title || !form.author || !form.summary) {
    saveError.value = 'Заповніть усі поля.'
    return
  }
  saving.value = true
  const request = id ? articlesApi.update(id, form) : articlesApi.create(form)
  await request
    .then(() => {
      notice.value = id ? 'Зміни збережено.' : 'Статтю додано.'
      return router.push({ name: 'articles' })
    })
    .catch((error) => { saveError.value = apiError(error) })
    .finally(() => { saving.value = false })
}

onMounted(() => {
  if (id) load()
})
</script>

<template>
  <div class="container shell page">
    <div class="form-shell">
      <RouterLink class="text-link back-link" to="/articles">← До каталогу</RouterLink>
      <div class="page-heading">
        <div>
          <p class="eyebrow">Каталог / {{ id ? 'Редагування' : 'Новий запис' }}</p>
          <h1>{{ id ? 'Редагувати статтю' : 'Додати статтю' }}</h1>
          <p class="page-lead">Назва, автор і короткий опис зберігаються в каталозі.</p>
        </div>
      </div>
      <div v-if="loading" class="status-panel" role="status">Завантажуємо статтю…</div>
      <div v-else-if="loadError" class="status-panel">
        <p role="alert">{{ loadError }}</p>
        <button class="btn btn-outline-secondary" type="button" @click="load">Повторити запит</button>
      </div>
      <form v-else class="card editor-card" @submit.prevent="save">
        <div class="card-body">
          <p v-if="saveError" class="alert alert-danger" role="alert">{{ saveError }}</p>
          <fieldset :disabled="saving">
            <div class="mb-3">
              <label class="form-label" for="article-title">Назва</label>
              <input id="article-title" v-model.trim="form.title" class="form-control" required maxlength="120" placeholder="Назва статті">
            </div>
            <div class="mb-3">
              <label class="form-label" for="article-author">Автор</label>
              <input id="article-author" v-model.trim="form.author" class="form-control" required maxlength="80" placeholder="Ім’я та прізвище">
            </div>
            <div class="mb-4">
              <label class="form-label" for="article-summary">Короткий опис</label>
              <textarea id="article-summary" v-model.trim="form.summary" class="form-control" required maxlength="1000" rows="4" placeholder="Про що ця стаття?"></textarea>
            </div>
            <div class="d-flex flex-wrap gap-2">
              <button class="btn btn-primary" type="submit">{{ saving ? 'Зберігаємо…' : (id ? 'Зберегти зміни' : 'Додати до каталогу') }}</button>
              <RouterLink v-if="!saving" class="btn btn-outline-secondary" to="/articles">Скасувати</RouterLink>
            </div>
          </fieldset>
        </div>
      </form>
    </div>
  </div>
</template>
