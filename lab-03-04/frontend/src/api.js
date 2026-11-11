import axios from 'axios'

const api = axios.create({ baseURL: '/api', timeout: 6000 })

export const articlesApi = {
  list: () => api.get('/articles').then(({ data }) => data),
  get: (id) => api.get('/articles/' + id).then(({ data }) => data),
  create: (article) => api.post('/articles', article).then(({ data }) => data),
  update: (id, article) => api.put('/articles/' + id, article).then(({ data }) => data),
  remove: (id) => api.delete('/articles/' + id),
}

export function apiError(error) {
  const status = error.response?.status
  if (!status || [502, 503, 504].includes(status)) {
    return 'Не вдалося з’єднатися із сервером. Перевірте, чи запущено сервер, і спробуйте ще раз.'
  }
  if (status === 404) return 'Статтю не знайдено. Можливо, її вже видалено.'
  if (status === 400) {
    return error.response.data?.errors?.join(' ') || 'Перевірте введені дані.'
  }
  return 'Сервер не зміг обробити запит. Спробуйте ще раз.'
}
