<template>
  <div class="container shell page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">Лабораторні роботи 03 / 04</p>
        <h1>Про проєкт</h1>
        <p class="page-lead">Навчальний застосунок, який показує шлях даних від браузера до бази.</p>
      </div>
    </div>
    <div class="row g-3 mb-4">
      <div class="col-12 col-md-4"><article class="card guide-card h-100"><div class="card-body">
        <span class="guide-index">01</span><h2>Vue 3</h2><p>Інтерфейс на Composition API. Vue Router перемикає сторінки, а Axios надсилає запити.</p>
      </div></article></div>
      <div class="col-12 col-md-4"><article class="card guide-card h-100"><div class="card-body">
        <span class="guide-index">02</span><h2>Spring Boot</h2><p>REST API приймає запити, перевіряє дані й виконує операції з каталогом.</p>
      </div></article></div>
      <div class="col-12 col-md-4"><article class="card guide-card h-100"><div class="card-body">
        <span class="guide-index">03</span><h2>PostgreSQL</h2><p>Таблиця articles зберігає назву, автора й опис кожної статті.</p>
      </div></article></div>
    </div>
    <section class="card api-card" aria-labelledby="api-title">
      <div class="card-body">
        <h2 id="api-title">Операції REST API</h2>
        <div class="table-responsive">
          <table class="table align-middle mb-0">
            <thead><tr><th scope="col">Метод</th><th scope="col">Шлях</th><th scope="col">Дія</th></tr></thead>
            <tbody>
              <tr><th scope="row">GET</th><td><code>/api/articles</code></td><td>Переглянути каталог</td></tr>
              <tr><th scope="row">GET</th><td><code>/api/articles/{id}</code></td><td>Отримати одну статтю</td></tr>
              <tr><th scope="row">POST</th><td><code>/api/articles</code></td><td>Додати статтю</td></tr>
              <tr><th scope="row">PUT</th><td><code>/api/articles/{id}</code></td><td>Оновити статтю</td></tr>
              <tr><th scope="row">DELETE</th><td><code>/api/articles/{id}</code></td><td>Видалити статтю</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>
  </div>
</template>
