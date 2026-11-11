<script setup>
import { computed, onMounted, ref } from 'vue'
import AnalyticsChart from '../components/AnalyticsChart.vue'
import { analyticsApi, apiError } from '../api.js'

const languageNames = {
  ukr: 'Українська',
  eng: 'Англійська',
  other: 'Інші мови',
  unknown: 'Не вказано',
}
const templateUrl = import.meta.env.BASE_URL + 'template.xlsx'
const fileInput = ref(null)
const file = ref(null)
const importing = ref(false)
const loading = ref(false)
const statistics = ref(null)
const importError = ref('')
const analyticsError = ref('')
const result = ref(null)

const languages = computed(() => (statistics.value?.languages ?? []).map(({ label, count }) => ({
  code: label,
  label: languageNames[label] ?? label,
  count,
})))

async function loadAnalytics() {
  loading.value = true
  analyticsError.value = ''
  try {
    statistics.value = await analyticsApi.get()
  } catch (error) {
    analyticsError.value = apiError(error)
  } finally {
    loading.value = false
  }
}

function selectFile(event) {
  file.value = event.target.files[0] ?? null
  importError.value = ''
  result.value = null
}

async function importFile() {
  if (!file.value || importing.value) return
  importError.value = ''
  result.value = null
  if (!file.value.name.toLowerCase().endsWith('.xlsx')) {
    importError.value = 'Виберіть файл з розширенням .xlsx.'
    return
  }
  if (file.value.size > 2 * 1024 * 1024) {
    importError.value = 'Розмір файлу має бути не більше 2 МБ.'
    return
  }

  importing.value = true
  try {
    const { imported } = await analyticsApi.importExcel(file.value)
    result.value = imported
    file.value = null
    if (fileInput.value) fileInput.value.value = ''
    await loadAnalytics()
  } catch (error) {
    importError.value = apiError(error)
  } finally {
    importing.value = false
  }
}

onMounted(loadAnalytics)
</script>

<template>
  <div class="container shell page analytics-page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">Читальня / Лабораторна 06</p>
        <h1>Імпорт та аналітика</h1>
        <p class="page-lead">Додайте статті з Excel і перегляньте статистику каталогу.</p>
      </div>
      <div v-if="statistics" class="analytics-total">
        <strong data-testid="article-total">{{ statistics.total }}</strong>
        <span>Статей у каталозі</span>
      </div>
    </div>

    <section class="card import-card" aria-labelledby="import-title">
      <div class="card-body">
        <div class="import-heading">
          <div><p class="eyebrow">01 / Завантаження</p><h2 id="import-title">Імпорт з Excel</h2></div>
          <a class="btn btn-outline-secondary" :href="templateUrl" download="template.xlsx">Завантажити шаблон</a>
        </div>
        <form class="import-form" @submit.prevent="importFile">
          <div>
            <label for="excel-file" class="form-label">Файл Excel</label>
            <input id="excel-file" ref="fileInput" class="form-control" type="file"
              accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
              aria-describedby="file-hint" :disabled="importing" @change="selectFile">
          </div>
          <button type="submit" class="btn btn-primary" :disabled="!file || importing || loading">
            {{ importing ? 'Імпортуємо…' : 'Імпортувати' }}
          </button>
        </form>
        <p id="file-hint" class="import-hint">.xlsx до 2 МБ · до 2 000 рядків · перший аркуш із заголовками</p>
        <p v-if="importError" class="alert alert-danger mb-0 mt-3" role="alert">{{ importError }}</p>
        <p v-if="result !== null" class="alert alert-success mb-0 mt-3" role="status">Імпортовано записів: {{ result }}.</p>
      </div>
    </section>

    <section class="analytics-section" aria-labelledby="stats-title">
      <div class="import-heading">
        <div><p class="eyebrow">02 / Статистика</p><h2 id="stats-title">Огляд каталогу</h2></div>
        <RouterLink class="text-link" to="/articles">Перейти до статей</RouterLink>
      </div>
      <p v-if="loading" class="status-panel" role="status">Оновлюємо статистику…</p>
      <div v-if="analyticsError" class="status-panel mb-3">
        <p role="alert">{{ statistics ? 'Статистику не оновлено. ' : '' }}{{ analyticsError }}</p>
        <button class="btn btn-outline-secondary" type="button" :disabled="loading || importing" @click="loadAnalytics">Повторити запит</button>
      </div>
      <div v-if="statistics?.total" class="row g-3">
        <div class="col-12 col-lg-7">
          <article class="card analytics-card h-100">
            <div class="card-body">
              <h3>Статті за категоріями</h3>
              <AnalyticsChart type="bar" :rows="statistics.categories" label="Кількість статей за категоріями" />
            </div>
          </article>
        </div>
        <div class="col-12 col-lg-5">
          <article class="card analytics-card h-100">
            <div class="card-body">
              <h3>Мови статей</h3>
              <AnalyticsChart type="pie" :rows="languages" label="Частка статей за мовами" />
            </div>
          </article>
        </div>
      </div>
      <p v-else-if="statistics && !loading" class="status-panel">Каталог порожній. Імпортуйте файл, щоб побачити графіки.</p>
    </section>
  </div>
</template>
