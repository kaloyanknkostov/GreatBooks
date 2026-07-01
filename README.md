# GoodBooks

**Discover, rate, and get personalized book recommendations** — a Goodreads-inspired platform built as a modern, stateless REST API.

GoodBooks is a full-stack book community backend: browse a catalog of thousands of titles, track what you read, leave ratings and reviews, and receive recommendations tailored to your taste. The API is designed for a separate web client and scales horizontally with no server-side sessions.

---

## What you get

| Capability | Description |
|------------|-------------|
| **Book catalog** | Search and browse books with pagination, tags, and cover art from external sources |
| **Ratings & reviews** | Users rate books; aggregate scores stay consistent under concurrent updates |
| **Personal shelves** | Reading lists and activity without tying state to a single server instance |
| **Smart recommendations** | Content-based and collaborative filtering via a dedicated Python service |
| **Production-ready data** | PostgreSQL backed by the [goodbooks-10k-extended](https://github.com/malcolmosh/goodbooks-10k-extended) dataset (~10k books, ~6M ratings, enriched metadata) |

---

## Architecture

```
┌─────────────┐     JSON/REST      ┌──────────────────┐
│  Web client │ ◄────────────────► │  Spring Boot API │
│  (planned)  │                    │     (GoodBooks)  │
└─────────────┘                    └────────┬─────────┘
                                            │
                         ┌──────────────────┼──────────────────┐
                         ▼                  ▼                  ▼
                   PostgreSQL        Open Library /        Python
                   (goodbooks10k)     cover images         recommendations
```

- **Stateless API** — JSON only; no Thymeleaf or server sessions  
- **Domain-driven packages** — `book`, `user`, `rating`, `recommendation`  
- **Clear boundaries** — external catalog and images behind proxies; core domains stay internal  

---

## Tech stack

| | |
|---|---|
| **Runtime** | Java 21 |
| **Framework** | Spring Boot 4 |
| **Build** | Gradle (Kotlin DSL) |
| **Database** | PostgreSQL + Spring Data JPA |
| **Recommendations** | Python microservice (OpenFeign) |
| **Frontend** | JavaScript SPA (separate repository, planned) |

---

## Quick start

**Requirements:** JDK 21, Git

```bash
git clone git@github.com:kaloyanknkostov/GoodBooks.git
cd GoodBooks
./gradlew bootRun
```

On startup, the app seeds a sample book and logs context wiring so you can confirm everything loaded.

```bash
./gradlew test
```

REST endpoints and the database layer are on the roadmap — see [PLAN.md](PLAN.md) for the full development plan and current progress.

---

## Project status

Early development: Spring context, domain models, repositories, and services are in place. Next up: AOP logging, REST API with pagination, PostgreSQL, and the recommendation service.

For architecture decisions, phased roadmap, and task checklists, see **[PLAN.md](PLAN.md)**.

---

## Learning context

This project follows [*Spring Start Here*](https://www.manning.com/books/spring-start-here) by Laurențiu Spilcă, adapted from an e-commerce tutorial into a book catalog and ratings domain.

## Data credits

Book catalog and ratings data are sourced from open datasets on GitHub:

- **[goodbooks-10k-extended](https://github.com/malcolmosh/goodbooks-10k-extended)** — extended book metadata (descriptions, page counts, publication dates, genres) by Malcolm Osh
- **[goodbooks-10k](https://github.com/zygmuntz/goodbooks-10k)** — original Goodreads ratings and catalog (~10k books, ~6M ratings) by Zygmunt Zając

## Links

- [Development plan & roadmap](PLAN.md)
- [Spring Boot documentation](https://docs.spring.io/spring-boot/)
