import axios from 'axios'

const api = axios.create({ baseURL: '/api', timeout: 6000 })

export const articlesApi = {
  list: () => api.get('/articles').then(({ data }) => data),
  get: (id) => api.get('/articles/' + id).then(({ data }) => data),
  create: (article) => api.post('/articles', article).then(({ data }) => data),
  update: (id, article) => api.put('/articles/' + id, article).then(({ data }) => data),
  remove: (id) => api.delete('/articles/' + id),
}

export const analyticsApi = {
  get: () => api.get('/analytics').then(({ data }) => data),
  importExcel: (file) => {
    const form = new FormData()
    form.append('file', file)
    return api.post('/import/excel', form, { timeout: 30000 }).then(({ data }) => data)
  },
}

export function apiError(error) {
  const status = error.response?.status
  if (!status || [502, 503, 504].includes(status)) {
    return 'Не вдалося з’єднатися із сервером. Перевірте, чи запущено сервер, і спробуйте ще раз.'
  }
  if (status === 404) return 'Статтю не знайдено. Можливо, її вже видалено.'
  if (status === 413) return 'Розмір файлу має бути не більше 2 МБ.'
  if (status === 400) {
    return error.response.data?.errors?.join(' ') || 'Перевірте введені дані.'
  }
  return 'Сервер не зміг обробити запит. Спробуйте ще раз.'
}
