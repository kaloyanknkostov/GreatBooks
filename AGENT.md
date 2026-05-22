# Goodreads Clone — Project Progress & Plan

## Overview
Building a production-like Spring Boot REST API for a Goodreads-inspired book platform, using:
- **Java 21** with Spring Boot (Kotlin DSL Gradle)
- **PostgreSQL** database (from day one — no H2 migration later)
- **Spring Data JPA** (Hibernate) for persistence
- **Python microservice** for recommendations (future chapter)
- **JavaScript frontend** (AI-assisted, separate repo)
- **Stateless API** — no server-side sessions, no Thymeleaf

---

## Completed ✅

### Project Initialization (Ch 1–5 Setup)
- [x] Generated Spring Boot project via IntelliJ IDEA
- [x] Gradle with Kotlin DSL (`build.gradle.kts`)
- [x] Java 21 toolchain
- [x] Lombok configured in build file
- [x] Lombok annotation processor wired correctly

### Domain Model
- [x] Created `com.example.bookstore.model.Book` (using Lombok `@Getter/@Setter`)
  - Fields: `id`, `title`, `author`, `price`, `quantity`

### Repository Layer — In Progress
- [x] Created `com.example.bookstore.repository.BookRepository` (interface)
  - Methods: `void addBook(Book book)`, `Book getBook(int id)`
- [x] Created `com.example.bookstore.repository.MockRepository` implementing `BookRepository`
  - Initialized `List<Book>` field
  - Added `@Repository` stereotype annotation
- [x] Created `com.example.bookstore.proxy.BookCatalogProxy` (interface, empty)

---

## Package Structure (Per-Domain)

Each domain gets its own package with model, repository, service, and proxy only if needed:

```
com.example.bookstore/
├── book/
│   ├── model/Book.java
│   ├── repository/BookRepository.java
│   ├── repository/MockBookRepository.java
│   ├── service/BookService.java
│   └── proxy/BookCatalogProxy.java          ← external metadata/images
│       proxy/ExternalBookCatalogProxy.java
├── user/
│   ├── model/User.java
│   ├── repository/UserRepository.java
│   └── service/UserService.java
├── rating/
│   ├── model/Rating.java
│   ├── repository/RatingRepository.java
│   └── service/RatingService.java
└── recommendation/
    └── service/RecommendationService.java
```

**Proxy rule:** Only create a proxy when there is a real external dependency or abstraction needed. `BookCatalogProxy` fetches external metadata (Open Library, cover images). `User` and `Rating` have no external dependency → no proxy needed.

---

## Architecture Decisions

| Decision | Rationale |
|----------|-----------|
| Skip Thymeleaf | Frontend is JS-based; API serves JSON only |
| Skip server-side sessions | API must be stateless and horizontally scalable |
| PostgreSQL from day one | Production parity; no schema migration pain later |
| Spring Data JPA (not JDBC) | Industry standard; handles relationships cleanly |
| Lombok | Reduces boilerplate; helps focus on Spring concepts |
| Gradle Kotlin DSL | Modern default; better IDE support |
| Kotlin DSL but Java code | Build config in Kotlin, app code in Java |
| Domain-per-package structure | `book/`, `user/`, `rating/`, `recommendation/` — scales with complexity |
| No empty proxies | Only `BookCatalogProxy` exists because it has a real external role |
| Derived fields synced on write | `User.avgRating`, `Book.overallRating`, etc. updated when a `Rating` is created/modified. Sync function to be designed later. |

---

## Modified Chapter Roadmap

### Phase 1: Core Spring Context & DI (Ch 1–5)
**Status:** In Progress
- [x] `BookRepository` interface and `MockBookRepository` implementation
- [x] `BookCatalogProxy` interface (replaces book's `DeliveryNotificationProxy`)
- [ ] `ExternalBookCatalogProxy` implementation (logs to console/mock URL for now)
- [ ] `BookService` with constructor injection (`@Service`) — catalog operations only
- [ ] Scoped bean: `BookImportProcessor` with `@Scope(PROTOTYPE)` (mutable state per import job)
- **Deviations from book:** E-commerce domain replaced with Goodreads catalog domain. No email proxy.

### Phase 2: AOP (Ch 6)
- Create custom `@LogExecutionTime` annotation
- Create `@Aspect` class with `@Around` advice on `BookService` methods
- Log execution time to console

### Phase 3: Spring Boot & REST (Ch 7–8, modified)
- Skip Thymeleaf entirely
- Add `spring-boot-starter-web`
- Create `@RestController` returning JSON
- Build endpoints: `GET /api/books`, `POST /api/books`
- Add pagination from day one (`Pageable`)

### Phase 4: Web Scopes (Ch 9, skipped/modified)
- Skip `@SessionScope` and `@RequestScope` beans
- Frontend handles cart/reading-list state
- Document why server-side sessions are an anti-pattern for this architecture

### Phase 5: REST API Design (Ch 10)
- Build proper REST endpoints with `@RequestBody`
- Domain exceptions (e.g., `BookNotFoundException`)
- Use `@RestControllerAdvice` + `@ExceptionHandler` for global error handling
- Return proper HTTP status codes

### Phase 6: External Service Integration (Ch 11, repurposed)
- Add Spring Cloud OpenFeign
- Create proxy to call Python recommendation microservice
- Endpoint: `GET /recommendations/{bookId}`
- Handle failure modes (timeout, service down)

### Phase 7: Database Layer (Ch 12–14, modified)
- Switch from `MockBookRepository` to real PostgreSQL persistence
- Add PostgreSQL driver + JPA dependencies
- Import `goodbooks10k` dataset into Postgres
- Add indexes on `book_id`, `user_id` for ratings table
- Implement pagination on book list endpoints
- **Note:** Consider brief `JdbcTemplate` exposure to understand the abstraction before full JPA

### Phase 8: Transactions (Ch 13)
- Annotate rating submission with `@Transactional`
- When a rating is saved, update derived fields (book avg, user avg) atomically
- Test rollback behavior if derived-field update fails

### Phase 9: Testing (Ch 15)
- JUnit 5 + Mockito unit tests for `BookService`
- `@SpringBootTest` integration tests with real (or test) PostgreSQL
- Test REST endpoints with `MockMvc` or `TestRestTemplate`

### Phase 10: Recommendations (Custom, Post-Book)
- Python microservice for book recommendations
- Tier 1: Content-based (genre/tag similarity), pre-computed
- Tier 2: Collaborative filtering with `surprise` (optional)
- Java Feign client calls Python service
- Cache recommendation results

---

## Current Open Tasks

1. **Refactor `MockRepository` → `book/repository/MockBookRepository.java`**
   - Move to `book` package
   - Rename class for clarity
   - Implement `addBook` (auto-assign sequential ID if unset)
   - Implement `getBook` (loop through list, match by ID)
   - Add `getAllBooks()` for listing

2. **Implement `BookCatalogProxy` and its mock implementation**
   - Decide method: `String getCoverImageUrl(Long bookId)` or `BookMetadata fetchMetadata(String isbn)`
   - Create `ExternalBookCatalogProxy` annotated with `@Component`
   - For now, return a hardcoded/mock string or print to console

3. **Create `book/service/BookService.java`**
   - Constructor injection: `BookRepository` + `BookCatalogProxy`
   - Methods:
     - `Book getBookById(int id)`
     - `List<Book> getAllBooks()`
     - `Book addBook(Book book)` — validates and delegates to repo
     - `List<Book> findBooksByAuthor(String author)`
   - No rating logic, no user logic — catalog only

4. **Create prototype-scoped bean**
   - `BookImportProcessor` in `book/service` package
   - Annotated with `@Scope(BeanDefinition.SCOPE_PROTOTYPE)`
   - Holds mutable state: `int processedCount`, `int failedCount`

---

## Notes & Reminders

- **Build command:** `./gradlew bootRun` (always use wrapper)
- **Package root:** `com.example.bookstore`
- **Book field `ID`:** Currently uppercase (`ID`). Standard convention is lowercase `id` with camelCase. Consider refactoring.
- **Price field:** Currently `double`. For production money handling, consider `BigDecimal` when switching to real persistence.
- **Frontend contract:** All endpoints must return JSON. No HTML views.
- **Dataset:** `goodbooks10k` — 10,000 books, ~6M ratings. Plan for pagination immediately.
- **Rating sync:** Derived fields (`Book.overallRating`, `User.avgRating`, `# of ratings`) will be updated every time a `Rating` is created/modified. Design this sync logic when `RatingService` is built.

---

## Resources

- Book: "Spring Start Here" by Laurențiu Spilcă
- Spring Boot version: 4.0.6 (Kotlin DSL, Java 21)
- Dataset: https://github.com/zygmuntz/goodbooks-10k
