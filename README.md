# Epigraph

Epigraph is a personal app for collecting and keeping quotes you love — from books, articles, conversations, or anywhere else. Open it and see a quote of the day. Add new ones, mark favourites, search by author or keyword, and export your collection any time.

Your quotes are stored in a database and never lost between sessions.

## Live app

→ [epigraph.up.railway.app](https://epigraph.up.railway.app)

---

## For developers

### Project structure

```
epigraph/
├── backend/          — Spring Boot + Gradle (REST API + frontend static resources)
├── README.md
├── RELEASE_POLICY.md
└── .gitignore
```

### Requirements

| Tool       | Version                         |
|------------|---------------------------------|
| Java       | 21+                             |
| Gradle     | 8.14 (via wrapper, `./gradlew`) |
| PostgreSQL | 14+                             |

### Local development

#### 1. Database

Start **Postgres.app** (menubar icon → Start) and create the database:

```bash
psql -U postgres
CREATE DATABASE epigraph;
\q
```

#### 2. Backend + Frontend

```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

The app starts at **http://localhost:8080** — frontend is included and served at the same address.

Verify the API is running:
```bash
curl http://localhost:8080/api/quotes
# Expected: [] (empty array — all good)
```

---

### Configuration

#### Local (`application-local.properties`)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/epigraph
spring.datasource.username=postgres
spring.datasource.password=postgres
```

> This file is in `.gitignore` and is never committed.

#### Production (Railway environment variables)

| Variable                 | Value                        |
|--------------------------|------------------------------|
| `DATABASE_URL`           | set automatically by Railway |
| `DB_USER`                | set automatically by Railway |
| `DB_PASSWORD`            | set automatically by Railway |
| `SPRING_PROFILES_ACTIVE` | `prod`                       |

---

### API

Base URL: `http://localhost:8080/api`

| Method   | Path           | Description           |
|----------|----------------|-----------------------|
| `GET`    | `/quotes`      | Get all quotes        |
| `POST`   | `/quotes`      | Add a quote           |
| `PUT`    | `/quotes/{id}` | Update a quote        |
| `DELETE` | `/quotes/{id}` | Delete a single quote |
| `DELETE` | `/quotes`      | Delete all quotes     |

#### Quote format (JSON)

```json
{
  "id": 1,
  "text": "Quote text",
  "author": "Author name",
  "source": "Book title",
  "fav": false,
  "tags": "philosophy,motivation",
  "added": 1712760000000
}
```

---

### Quick start

```bash
cd backend && ./gradlew bootRun --args='--spring.profiles.active=local'
```

Open **http://localhost:8080**
