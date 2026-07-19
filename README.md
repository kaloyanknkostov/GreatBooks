# GoodBooks

An educational testing platform for recommender systems, built on the [goodbooks-10k](https://github.com/zygmuntz/goodbooks-10k) catalog. Students implement a recommender against a real book community API, then watch it respond to live, simulated traffic — ratings, reads, and new recommendations — instead of scoring a frozen offline split.

---

## Motivation

I took a recommender systems course where almost everything stopped at the dataset. Train on goodbooks-10k, report RMSE or NDCG, ship the notebook. That teaches ranking metrics, but it skips the part that makes production recommenders interesting: the model lives in a loop. Users rate what you showed them. Taste drifts. New items appear. Yesterday’s good ranking can look wrong once people actually use the product.

I wanted a place where a student can plug in a goodbooks-10k recommender and *use* it — generate recommendations, feed them fake but realistic traffic in real time, and see how the system behaves as feedback rolls in. GoodBooks is that sandbox: a Goodreads-style application surface so you can study recommenders the way they behave online, not only the way they score offline.

---

## Architecture

```
┌─────────────┐     JSON/REST      ┌──────────────────┐
│  Web client │ ◄────────────────► │  Spring Boot API │
│             │                    │     (GoodBooks)  │
└─────────────┘                    └────────┬─────────┘
                                            │
                         ┌──────────────────┼──────────────────┐
                         ▼                  ▼                  ▼
                   PostgreSQL        Open Library /        Student
                   (goodbooks10k)     cover images         recommender
```

- **Stateless API** — JSON over HTTP; the app is the product surface your model talks to
- **Domain packages** — `book`, `user`, `rating`, and recommendation integration stay separated
- **Pluggable recommender** — your model runs as a separate service; the API feeds it catalog and interaction data and serves its rankings back to users (and to traffic simulation)

---

## Tech stack

| | |
|---|---|
| **Runtime** | Java 21 |
| **Framework** | Spring Boot |
| **Build** | Gradle (Kotlin DSL) |
| **Database** | PostgreSQL + Spring Data / JDBC + Flyway |
| **Recommendations** | Student-implemented service wired to the API |
| **Frontend** | Separate web client against the REST API |

---

## Quick Start

**Requirements:** JDK 21, PostgreSQL, Git

```bash
git clone git@github.com:kaloyanknkostov/GoodBooks.git
cd GoodBooks
```

Configure a local database in `src/main/resources/application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/greaterbooks
spring.datasource.username=YOUR_USER
spring.datasource.password=YOUR_PASSWORD
```

Then start the app:

```bash
./gradlew bootRun
```

Smoke-check the catalog:

```bash
curl http://localhost:8080/books/1
```

Run the test suite:

```bash
./gradlew test
```

---

## Usage

Once the app is up, treat it as the environment your recommender lives in.

**Browse and serve recommendations.** The Spring Boot API exposes the book catalog (and related user/rating flows) so a client — or your recommender service — can fetch titles, return ranked suggestions, and record feedback. Point your model at the same goodbooks-10k-backed data the app uses, then call into the API the way a production service would.

**Simulate traffic in real time.** Instead of freezing evaluation at a static test set, drive the app with fake users: ratings, shelf activity, and recommendation requests that arrive over time. Watch how your model’s outputs change as the stream of interactions grows. That closed loop — recommend → interact → update → recommend again — is the core exercise.

**Iterate on the model.** Swap algorithms (content-based, collaborative filtering, hybrids), change how cold-start users are handled, or retrain on newer interactions. Because the application keeps running, you can compare strategies under the same traffic patterns rather than only under a one-shot offline metric.

---

## Data credits

Book catalog and ratings data are sourced from open datasets on GitHub:

- **[goodbooks-10k](https://github.com/zygmuntz/goodbooks-10k)** — original Goodreads ratings and catalog (~10k books, ~6M ratings) by Zygmunt Zając
- **[goodbooks-10k-extended](https://github.com/malcolmosh/goodbooks-10k-extended)** — extended book metadata (descriptions, page counts, publication dates, genres) by Malcolm Osh

---

## Contributing

Issues and pull requests are welcome. Prefer small, focused changes that keep the platform useful as a recommender lab: clearer APIs for models, better traffic simulation hooks, or docs that help students get a first recommender online faster.

1. Fork the repo and create a branch from `main`
2. Make your change and add tests where they matter
3. Open a pull request describing what you changed and why

If you are using GoodBooks for a course assignment, feel free to open an issue describing the workflow you need — those shape the platform more than anything else.
