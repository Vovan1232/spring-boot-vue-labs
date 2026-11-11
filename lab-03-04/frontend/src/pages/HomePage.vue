<template>
  <div class="container shell page home-page">
    <section class="hero">
      <div class="hero-content">
        <p class="eyebrow hero-label">Електронний каталог</p>
        <h1>Читальня</h1>
        <p class="hero-copy">Усі статті в одному місці. Зберігайте назви, авторів і короткі описи того, що хочеться перечитати.</p>
        <div class="d-flex flex-wrap gap-2">
          <RouterLink class="btn btn-primary" to="/articles">Відкрити каталог <span aria-hidden="true">↗︎</span></RouterLink>
          <RouterLink class="btn btn-hero-outline" to="/articles/new">Додати статтю</RouterLink>
        </div>
      </div>
      <div class="hero-art" aria-hidden="true">
        <svg viewBox="0 0 360 258" fill="none">
          <circle cx="181" cy="129" r="110" stroke="#7b9b82" stroke-dasharray="2 6"/>
          <circle cx="181" cy="129" r="86" stroke="#62816c"/>
          <rect x="136" y="58" width="118" height="151" rx="7" transform="rotate(13 136 58)" fill="#a9b891" stroke="#243e32" stroke-width="3"/>
          <rect x="107" y="54" width="118" height="151" rx="7" transform="rotate(-9 107 54)" fill="#f2d6a4" stroke="#243e32" stroke-width="3"/>
          <path d="M134 85l71-11M137 104l71-11M140 123l51-8M143 142l63-10" stroke="#795e3f" stroke-width="2" stroke-linecap="round"/>
          <path d="M191 42l14-2 6 38-8-4-6 6-6-38Z" fill="#b65b3d"/>
          <path d="M73 100v19m-9-9.5h18m192 66v19m-9-9.5h18m-9-121v13m-6.5-6.5h13" stroke="#f4d7a2" stroke-width="1.8" stroke-linecap="round"/>
        </svg>
      </div>
    </section>
    <section class="home-guide" aria-labelledby="guide-title">
      <div class="d-flex justify-content-between align-items-end flex-wrap gap-2 section-heading">
        <div>
          <p class="eyebrow">Можливості каталогу</p>
          <h2 id="guide-title">Робота зі статтями</h2>
        </div>
        <RouterLink class="text-link" to="/about">Як це працює <span aria-hidden="true">↗︎</span></RouterLink>
      </div>
      <div class="row g-3">
        <div class="col-12 col-md-4"><div class="card guide-card h-100"><div class="card-body">
          <span class="guide-index">01</span><h3>Переглядайте</h3><p>Знаходьте статті за назвою, автором або фрагментом опису.</p>
        </div></div></div>
        <div class="col-12 col-md-4"><div class="card guide-card h-100"><div class="card-body">
          <span class="guide-index">02</span><h3>Додавайте</h3><p>Занотовуйте назву, ім’я автора й кілька слів про статтю.</p>
        </div></div></div>
        <div class="col-12 col-md-4"><div class="card guide-card h-100"><div class="card-body">
          <span class="guide-index">03</span><h3>Упорядковуйте</h3><p>Редагуйте записи й видаляйте ті, які більше не потрібні.</p>
        </div></div></div>
      </div>
    </section>
  </div>
</template>
