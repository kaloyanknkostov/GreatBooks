# GoodBooks — Development Plan

> Internal roadmap, architecture notes, and progress tracking. For the public project overview, see [README.md](README.md).

A production-style **Goodreads-inspired book platform** built as a stateless Spring Boot REST API. The backend serves JSON only — a separate JavaScript frontend and Python recommendation microservice are planned for later.

> Learning project following [*Spring Start Here*](https://www.manning.com/books/spring-start-here) by Laurențiu Spilcă, adapted from an e-commerce tutorial into a book catalog and ratings domain.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Build | Gradle (Kotlin DSL) |
| Persistence (planned) | PostgreSQL + Spring Data JPA |
| Boilerplate | Lombok |
| Recommendations (planned) | Python microservice via OpenFeign |
| Frontend (planned) | JavaScript (separate repo) |

## Architecture

```
┌─────────────┐     JSON/REST      ┌──────────────────┐
│  JS Client  │ ◄────────────────► │  Spring Boot API │
└─────────────┘                    └────────┬─────────┘
                                            │
                         ┌──────────────────┼──────────────────┐
                         ▼                  ▼                  ▼
                   PostgreSQL      Open Library /        Python Rec
                   (goodbooks10k)   cover images          microservice
```

### Design decisions

- **Stateless API** — no server-side sessions; horizontally scalable
- **No Thymeleaf** — JSON-only responses for a separate frontend
- **PostgreSQL from day one** — production parity, no H2 migration later
- **Domain-per-package layout** — `book/`, `user/`, `rating/`, `recommendation/`
- **Proxy pattern only where needed** — external book metadata and images (`BookImageFetch`); user/rating domains stay internal

## Project Structure

```
com.example.bookstore/
├── book/
│   ├── model/          Book, Tag
│   ├── repository/     BookRepository, BookMockRepository
│   ├── service/        BookService
│   └── proxy/          BookImageFetch (external metadata/images)
├── user/
│   └── model/          User
├── rating/             (planned)
└── recommendation/     (planned)
```

## Current Status

**Phase 1 — Core Spring Context & DI** ✅

- [x] Spring Boot project with Gradle Kotlin DSL and Java 21 toolchain
- [x] Lombok configured with annotation processing
- [x] `Book` domain model with tags
- [x] `BookRepository` interface + in-memory `BookMockRepository`
- [x] `BookService` with constructor injection
- [x] `BookImageFetch` proxy component for external catalog integration
- [x] `BookStartupTest` (`CommandLineRunner`) to verify Spring context wiring

**In progress**

- [ ] AOP execution-time logging (`@LogExecutionTime`, `@Aspect`)
- [ ] REST endpoints (`GET /api/books`, `POST /api/books`) with pagination
- [ ] Refactor `BookImageFetch` behind a `BookCatalogProxy` interface

## Getting Started

### Prerequisites

- JDK 21
- Git

### Run locally

```bash
git clone git@github.com:kaloyanknkostov/GoodBooks.git
cd GoodBooks
./gradlew bootRun
```

On startup, `BookStartupTest` seeds a sample book and prints context wiring output to the console.

### Run tests

```bash
./gradlew test
```

## Roadmap

| Phase | Focus |
|-------|-------|
| 1 | Spring DI, repositories, services ✅ |
| 2 | AOP — method execution logging |
| 3 | REST API with pagination |
| 4 | Error handling (`@RestControllerAdvice`, domain exceptions) |
| 5 | OpenFeign → Python recommendation service |
| 6 | PostgreSQL + JPA, [goodbooks-10k](https://github.com/zygmuntz/goodbooks-10k) dataset |
| 7 | Transactions — atomic rating updates with derived fields |
| 8 | JUnit 5, Mockito, `@SpringBootTest`, MockMvc |
| 9 | Recommendation tiers — content-based + optional collaborative filtering |

## Dataset

The production database will use the **[goodbooks-10k](https://github.com/zygmuntz/goodbooks-10k)** dataset (~10,000 books, ~6M ratings). List endpoints will support pagination from the start.

## Resources

- [Spring Start Here](https://www.manning.com/books/spring-start-here) — Laurențiu Spilcă
- [Spring Boot 4.0 docs](https://docs.spring.io/spring-boot/)
- [goodbooks-10k dataset](https://github.com/zygmuntz/goodbooks-10k)
