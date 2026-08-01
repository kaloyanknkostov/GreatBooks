# AGENTS.md

## Cursor Cloud specific instructions

GoodBooks is a single Spring Boot (Java 21, Gradle Kotlin DSL) app backed by PostgreSQL. Standard commands live in `README.md`; the notes below cover only non-obvious, environment-specific caveats.

### Services

| Service | Required | How to start | Notes |
|---|---|---|---|
| PostgreSQL 16 | Yes | `sudo pg_ctlcluster 16 main start` | NOT auto-started on boot — start it once per session before running the app, tests, or the data loader. |
| GoodBooks Spring Boot app | Yes | `./gradlew bootRun` | Serves on port `8080`. Flyway auto-runs `V1__initial_schema.sql` on startup. |

Database is `greaterbooks`, role `goodbooks` / password `goodbooks` (local, password auth over `localhost:5432`).

### Config files (gitignored — recreate if missing)

The active Spring profile is hard-coded to `local` (`spring.profiles.active=local`), so the app, tests, and data loader all require an `application-local.properties`. Two copies are needed (both gitignored via `**/application-local.properties`):

- `src/main/resources/application-local.properties`
- `src/scripts/resources/application-local.properties`

Each must contain:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/greaterbooks
spring.datasource.username=goodbooks
spring.datasource.password=goodbooks
```

Without these files the app/test/loader fail to start (no datasource beans).

### Dataset (in the `data/goodbooks-10k-extended/` submodule-boundary directory)

`data/goodbooks-10k-extended` is committed as an orphaned gitlink (no `.gitmodules`), so files placed there are untracked by the parent repo and simply live on disk. Flyway only creates empty tables; meaningful data requires these CSVs (all `*.csv` are gitignored):

- `books_enriched.csv` — from `malcolmosh/goodbooks-10k-extended` (`master`)
- `tags.csv`, `book_tags.csv`, `ratings.csv`, `to_read.csv` — from `zygmuntz/goodbooks-10k` (`master`)

Non-obvious gotcha: the tags loader (`SimpleTableInsertion`) expects `tags.csv` to have header `id,tag_name`, but the upstream file ships as `tag_id,tag_name`. Rename the first column to `id` before loading (`sed -i '1s/^tag_id,/id,/' tags.csv`).

### Loading data

`./gradlew runScript` runs `TestingDbConnection` → books, then tags/book_tags, then ratings/to_read (~2 min for the full ~6M ratings). Requires PostgreSQL running and the CSVs present. Loads are idempotent-ish (`INSERT ... ON CONFLICT DO NOTHING`), and books are committed before later steps, so the book endpoints work even if a later insertion step fails.

### Lint / Test / Build / Run

- Lint: no linter/formatter/static-analysis plugin is configured.
- Test: `./gradlew test` — the only test is `contextLoads` (`@SpringBootTest`), which boots the full context and therefore needs PostgreSQL running + `application-local.properties`.
- Build: `./gradlew build` (add `-x test` to skip the DB-dependent test).
- Run: `./gradlew bootRun` (port 8080).

### Endpoints (for smoke testing)

- `GET /books/{id}` — JSON book (e.g. `curl http://localhost:8080/books/1` → Harry Potter). `id<=0` → 400, unknown id → 404.
- `GET /book/{id}` — server-rendered HTML detail page (Thymeleaf).
- `GET /search?q=<query>` — HTML search results (title `ILIKE %q%`).
