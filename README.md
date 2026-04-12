# Epigraph

Личное приложение для хранения любимых цитат. Открываешь — видишь цитату дня. Добавляешь вручную или импортируешь из JSON. Цитаты сохраняются в PostgreSQL и не теряются при перезапуске.

## Структура проекта

```
epigraph/
├── backend/        — Spring Boot + Gradle (REST API)
├── frontend/       — Статический HTML/CSS/JS (также раздаётся Spring Boot из resources/static)
├── README.md
└── .gitignore
```

## Требования

| Инструмент | Версия                                 |
|------------|----------------------------------------|
| Java       | 21+                                    |
| Gradle     | 8.14 (через враппер, `./gradlew`)      |
| PostgreSQL  | 14+                                    |
| Python     | 3.x (опционально, для отдельного dev-сервера фронтенда) |

## Локальная разработка

### 1. База данных

Запусти **Postgres.app** (иконка в menubar → Start) и создай базу:

```bash
psql -U postgres
CREATE DATABASE epigraph;
\q
```

База данных будет доступна на `localhost:5432`.

### 2. Бекенд

```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

Приложение запустится на **http://localhost:8080** — фронтенд уже включён и открывается по этому же адресу.

Проверка API:
```bash
curl http://localhost:8080/api/quotes
# Ответ: [] (пустой массив — всё работает)
```

### 3. Фронтенд (опционально, для разработки с hot-reload)

Если нужно редактировать `index.html` и сразу видеть изменения без пересборки:

```bash
cd frontend
python3 -m http.server 3000
```

Открой браузер: **http://localhost:3000**

> ⚠️ Открывай именно через `http://localhost:3000`, а не как `file://` — браузер блокирует API-запросы из файловой системы.

> ℹ️ 404 для `/favicon.ico` в логах Python-сервера — это нормально, браузер запрашивает его автоматически.

***

## Конфигурация

### Переменные окружения (локально)

Настройки для локальной разработки задаются в `backend/src/main/resources/application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/epigraph
spring.datasource.username=postgres
spring.datasource.password=postgres
cors.allowed-origins=http://localhost:3000
```

> Этот файл добавлен в `.gitignore` — не попадает в репозиторий.

### Переменные окружения (продакшн, Railway)

Задаются в панели Variables на Railway:

| Переменная               | Значение                          |
|--------------------------|-----------------------------------|
| `DATABASE_URL`           | автоматически от Railway Postgres |
| `DB_USER`                | автоматически от Railway Postgres |
| `DB_PASSWORD`            | автоматически от Railway Postgres |
| `SPRING_PROFILES_ACTIVE` | `prod`                            |

***

## API

Базовый URL: `http://localhost:8080/api`

| Метод    | Путь           | Описание                           |
|----------|----------------|------------------------------------|
| `GET`    | `/quotes`      | Получить все цитаты                |
| `POST`   | `/quotes`      | Добавить цитату                    |
| `PUT`    | `/quotes/{id}` | Обновить цитату (в т.ч. избранное) |
| `DELETE` | `/quotes/{id}` | Удалить одну цитату                |
| `DELETE` | `/quotes`      | Удалить все цитаты                 |

### Формат цитаты (JSON)

```json
{
  "id": 1,
  "text": "Текст цитаты",
  "author": "Имя автора",
  "source": "Название книги",
  "fav": false,
  "tags": "философия,мотивация",
  "added": 1712760000000
}
```

***

## Деплой на Railway

1. Зарегистрируйся на [railway.app](https://railway.app) через GitHub
2. **New Project → Deploy from GitHub repo** → выбери `epigraph`, папка `backend`
3. **Add Service → Database → PostgreSQL** — Railway сам создаст `DATABASE_URL`
4. В настройках сервиса укажи переменную: `SPRING_PROFILES_ACTIVE=prod`
5. Railway автоматически обнаружит `build.gradle` и соберёт проект

***

## Быстрый старт

```bash
cd backend && ./gradlew bootRun --args='--spring.profiles.active=local'
```

Открой **http://localhost:8080**
