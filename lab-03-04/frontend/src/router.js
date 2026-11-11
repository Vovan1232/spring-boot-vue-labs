import { createRouter, createWebHistory } from 'vue-router'
import HomePage from './pages/HomePage.vue'
import ArticlesPage from './pages/ArticlesPage.vue'
import ArticleFormPage from './pages/ArticleFormPage.vue'
import AboutPage from './pages/AboutPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomePage, meta: { title: 'Головна' } },
    { path: '/articles', name: 'articles', component: ArticlesPage, meta: { title: 'Каталог' } },
    { path: '/articles/new', name: 'new', component: ArticleFormPage, meta: { title: 'Нова стаття' } },
    { path: '/articles/:id(\\d+)/edit', name: 'edit', component: ArticleFormPage, meta: { title: 'Редагування' } },
    { path: '/about', name: 'about', component: AboutPage, meta: { title: 'Про проєкт' } },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
  scrollBehavior: () => ({ top: 0 }),
})

router.afterEach((to) => {
  document.title = to.meta.title + ' · Читальня'
})

export default router
