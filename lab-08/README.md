# Лабораторна робота № 8 · Підготовка розгортання

Застосунок лабораторної № 7 адаптовано для Linux-сервера з PostgreSQL та Nginx. Використано варіант Docker. Контейнери запускають Spring Boot і PostgreSQL, а Nginx та Certbot працюють на сервері Ubuntu.

## Підготовлені компоненти

- `application-prod.properties` використовує PostgreSQL, Flyway, Hibernate validate і захищені cookie сесії.
- `Dockerfile` збирає JAR на Java 21 і запускає його від окремого користувача.
- `docker-compose.yml` задає автоперезапуск, перевірку працездатності та обмеження журналів.
- База даних зберігається в Docker volume. Для застосунку створюється користувач science_app без прав суперкористувача, створення баз або ролей.
- Порт 8080 публікується тільки на 127.0.0.1, PostgreSQL не публікує порт на сервері.
- Nginx перенаправляє HTTP на HTTPS та передає заголовки клієнта і протоколу.
- Certbot отримує сертифікат Let's Encrypt і поновлює його через таймер.

## Що потрібно на віддаленому сервері

Окремий сервер Ubuntu 22.04+ з 1 ядром, 2 ГБ RAM, доступом SSH на порту 22 та sudo. Потрібен домен із DNS-записом A на IP сервера. Якщо для домену існує AAAA, він також має вести на цей сервер. У мережевих правилах хостингу мають бути дозволені TCP 22, 80 і 443.

## Завантаження та запуск

Розпакуйте проєкт у папку `project-science` на сервері. Наприклад, з комп’ютера з підготовленим каталогом:

```sh
scp -r project-science USER@SERVER_IP:~/
ssh USER@SERVER_IP
cd ~/project-science
sudo bash deploy/install-server.sh YOUR_DOMAIN YOUR_EMAIL
```

Замініть USER, SERVER_IP, YOUR_DOMAIN і YOUR_EMAIL своїми значеннями. Скрипт призначено для окремого навчального сервера. Він встановлює Docker, Nginx, Certbot і UFW, запускає контейнери, відкриває порти 22/80/443 та запитує сертифікат Let's Encrypt. Випуск сертифіката включає прийняття умов Let's Encrypt для вказаної адреси електронної пошти.

Секрети генеруються у файлі `.env` з правами 600 командою `deploy/prepare-env.sh`. Файл виключений із Git і Docker build context. При повторному запуску наявні секрети зберігаються.

Після завершення відкрийте `https://YOUR_DOMAIN/`. Зареєструйте користувача. Щоб надати йому права адміністратора, на сервері виконайте:

```sh
sudo bash deploy/grant-admin.sh YOUR_USERNAME
```

Після призначення ролі вийдіть з акаунту та увійдіть знову. Демоакаунти у production-профілі не створюються.

## Перевірка на сервері

```sh
sudo docker compose ps
sudo docker compose logs --tail=100 app
curl --fail http://127.0.0.1:8080/actuator/health
curl --head https://YOUR_DOMAIN/
sudo nginx -t
sudo ufw status
sudo ss -ltnp
sudo certbot renew --dry-run
```

Після входу перевірте cookie JSESSIONID у засобах розробника браузера. Очікуються Secure, HttpOnly та SameSite=Lax. Для перевірки ліміту сесій увійдіть під тим самим логіном в іншому браузері, потім оновіть перший кабінет.

Actuator health доступний локально на 8080, а Nginx закриває зовнішній маршрут /actuator/. Сесії зберігаються в пам’яті одного процесу. Після перезапуску застосунку потрібно увійти знову. Облікові записи залишаються у PostgreSQL.

## Обслуговування

```sh
sudo docker compose logs -f app
sudo docker compose up -d --build --wait
sudo docker compose restart app
sudo systemctl status nginx certbot.timer
```

Команда `docker compose down` зупиняє проєкт зі збереженням тому бази. Для повторного запуску використовуйте `docker compose up -d`.

## Виконані перевірки

Конфігурацію Docker Compose перевірено командою config. JAR зібрано Maven. Локальна перевірка використовує справжні PostgreSQL, production-профіль і Nginx для Windows. Для HTTPS використано тимчасовий локальний сертифікат. Скриншоти та результати цієї перевірки містяться у папці screenshots.

На цьому етапі немає підтвердження запуску контейнерів на Linux, зовнішньої доступності домену, налаштування UFW на сервері та випуску сертифіката Let's Encrypt. Для завершення цих перевірок потрібні сервер і домен.

## Документація

- [Встановлення Docker на Ubuntu](https://docs.docker.com/engine/install/ubuntu/)
- [Заголовки Nginx Reverse Proxy](https://nginx.org/en/docs/http/ngx_http_proxy_module.html)
- [Certbot](https://certbot.eff.org/instructions?ws=nginx&os=snap)

