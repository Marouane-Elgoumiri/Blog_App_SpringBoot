<div align="center">

# CogniPost Backend

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/)
[![Postgres](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Intellij Idea](https://img.shields.io/badge/IntelliJ_IDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)](https://www.jetbrains.com/idea/)
[![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)](https://www.postman.com/)

[![Testing Library](https://img.shields.io/badge/Testing%20Library-E33332.svg?style=for-the-badge&logo=Testing-Library&logoColor=white)](https://testing-library.com/)
[![JUnit5](https://img.shields.io/badge/JUnit5-25A162.svg?style=for-the-badge&logo=JUnit5&logoColor=white)](https://junit.org/junit5/)

</div>

---

## Overview

**CogniPost** is a full-featured blog platform REST API built with Spring Boot 3.2.4 and Java 17. It provides a complete backend for a modern blogging application with JWT authentication, article management, nested comments, social interactions, personalized feeds, and full-text search. Designed to be consumed by a Next.js SSR frontend.

---

## Architecture

```
com.example.blog_app_springboot/
├── config/              # SecurityConfig, CorsConfig, OpenApiConfig, JpaConfig
├── common/
│   ├── base/            # BaseEntity with JPA auditing
│   ├── dtos/            # ApiResponse<T>, PageResponse<T>, ErrorResponse
│   ├── exceptions/      # GlobalExceptionHandler, custom exceptions
│   ├── utils/           # SlugGenerator, ReadingTimeCalculator
├── security/
│   ├── jwt/             # JwtTokenProvider, JwtAuthFilter
│   ├── config/          # SecurityFilterChain, CustomAuthenticationEntryPoint
│   ├── auth/            # AuthService, AuthController
│   └── util/            # SecurityUtil
├── users/               # User management with BCrypt + roles
├── articles/            # Full CRUD with drafts, tags, pagination
├── comments/            # Nested comments with cascade delete
├── tags/                # Auto-created tags, many-to-many with articles
├── interactions/        # Likes & Bookmarks with toggle logic
├── follows/             # User follow system with events
├── feed/                # Personalized feed from followed users
├── search/              # JPA Specifications + PostgreSQL full-text search
└── notifications/       # Spring Events, async email service
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Framework** | Spring Boot 3.2.4, Java 17 |
| **Database** | H2 (dev), PostgreSQL (prod) |
| **Auth** | JWT (jjwt 0.12.5), BCrypt, Spring Security |
| **ORM** | Spring Data JPA, Hibernate 6.4 |
| **Validation** | Bean Validation (Hibernate Validator) |
| **API Docs** | springdoc OpenAPI 2.5 (Swagger UI) |
| **Rate Limiting** | Bucket4j 8.10.1 |
| **Testing** | JUnit 5, Mockito, MockMvc, @DataJpaTest |
| **Build** | Gradle |

## Design Patterns

| Pattern | Implementation |
|---------|---------------|
| **Repository** | Spring Data JPA interfaces with custom queries |
| **Service Layer** | Interface + implementation per module |
| **DTO Pattern** | Request/Response DTOs decoupled from entities |
| **Strategy** | Search strategies (PostgreSQL tsvector vs LIKE fallback) |
| **Observer** | Spring `@EventListener` for domain events (comments, follows) |
| **Specification** | JPA `Specification` for composable dynamic queries |
| **Builder** | Lombok `@SuperBuilder` on all entities and DTOs |
| **Decorator** | Rate limiting filter (Bucket4j) |

## Security

- **JWT stateless auth** — access token (15min) + refresh token (7 days)
- **BCrypt** password hashing
- **Role-based access** — `ROLE_USER`, `ROLE_ADMIN`
- **Method-level security** — `@PreAuthorize` on protected endpoints
- **CORS** configured for Next.js frontend (`localhost:3000`)
- **Input validation** — `@Valid` + Bean Validation on all DTOs
- **Token blacklist** — in-memory logout invalidation
- **Custom error handlers** — `AuthenticationEntryPoint` (401), `AccessDeniedHandler` (403)

---

## API Endpoints

**Base URL:** `/api/v1`

### Authentication
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/auth/login` | No | Login with username + password |
| `POST` | `/auth/refresh` | No | Refresh JWT tokens |
| `POST` | `/auth/logout` | Token | Blacklist current token |

### Users
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/users` | No | Register new user |
| `GET` | `/users/{id}` | No | Get user profile |
| `GET` | `/users/me` | Yes | Get current user |
| `GET` | `/users/me/stats` | Yes | Author dashboard stats |
| `POST` | `/users/{id}/follow` | Yes | Toggle follow |

### Articles
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/articles` | No | List published articles (paginated) |
| `GET` | `/articles/{slug}` | No | Get article by slug |
| `GET` | `/articles/search` | No | Search articles (q, tag, author) |
| `GET` | `/articles/popular` | No | Popular articles |
| `POST` | `/articles` | Yes | Create article (draft) |
| `PUT` | `/articles/{id}` | Owner | Update article |
| `DELETE` | `/articles/{id}` | Owner | Delete article |
| `GET` | `/articles/my` | Yes | Get my articles (incl. drafts) |

### Comments
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/articles/{slug}/comments` | Yes | Create comment |
| `GET` | `/articles/{slug}/comments` | No | Get all comments (nested) |
| `GET` | `/articles/{slug}/comments/{id}/replies` | No | Get replies |
| `DELETE` | `/articles/{slug}/comments/{id}` | Owner | Delete comment |

### Tags
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/tags` | No | List all tags |
| `GET` | `/tags/{slug}` | No | Get tag by slug |
| `POST` | `/tags` | Admin | Create tag |
| `DELETE` | `/tags/{id}` | Admin | Delete tag |

### Interactions
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/articles/{slug}/like` | Yes | Toggle like |
| `POST` | `/articles/{slug}/bookmark` | Yes | Toggle bookmark |

### Feed
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/feed` | Yes | Personalized feed |

---

## Response Format

All responses are wrapped in a standardized envelope:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-04-04T21:22:42.873991"
}
```

Paginated responses use `PageResponse<T>`:

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5,
  "last": false
}
```

---

## Getting Started

### Prerequisites
- Java 17+
- Gradle 8.x
- PostgreSQL (for production)

### Quick Start

```bash
# Clone the repository
git clone https://github.com/your-org/CogniPost.git
cd CogniPost

# Run with dev profile (H2 database)
./gradlew bootRun

# Run with prod profile (PostgreSQL)
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### Configuration

| Profile | Database | Port |
|---------|----------|------|
| `dev` | H2 (file-based) | 8080 |
| `prod` | PostgreSQL | ${PORT} |
| `test` | H2 (in-memory) | - |

**H2 Console (dev):** `http://localhost:8080/h2-console`

**Swagger UI:** `http://localhost:8080/swagger-ui.html`

### Environment Variables (prod)

| Variable | Description |
|----------|-------------|
| `DB_URL` | PostgreSQL connection URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | JWT signing key (min 256 bits) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins |

---

## Testing

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Run specific test class
./gradlew test --tests "AuthIntegrationTest"
```

**Test Coverage:** 95+ unit and integration tests covering:
- Repository layer (`@DataJpaTest`)
- Service layer (Mockito unit tests)
- Integration tests (`@SpringBootTest` + MockMvc)
- Security tests (auth flow, token validation, role-based access)

---

## Database Schema

```
users ──────────────┐
  ├── id            │
  ├── username      │
  ├── password      │
  ├── email         │
  ├── bio           │
  ├── image         │
  └── roles         │
                    │
articles ───────────┤
  ├── id            │
  ├── title         │
  ├── slug          │
  ├── subtitle      │
  ├── body          │
  ├── status        │
  ├── author_id ────┘
  └── tags ────────┐
                   │
comments           │
  ├── id           │
  ├── title        │
  ├── body         │
  ├── article_id ──┘
  ├── author_id ───┐
  └── parent_id    │
                   │
tags               │
  ├── id           │
  ├── name         │
  └── slug         │
                   │
likes              │
  ├── user_id ─────┘
  └── article_id ──┘

bookmarks          │
  ├── user_id ─────┘
  └── article_id ──┘

follows            │
  ├── follower_id ─┘
  └── following_id ┘
```

---

## API Versioning

This API uses URL-based versioning with the `/api/v1/` prefix.

### Current Version: v1

All endpoints are prefixed with `/api/v1/`. This versioning strategy allows for:
- **Backward compatibility** — existing clients continue to work with v1
- **Parallel versions** — future versions (v2, v3) can coexist alongside v1
- **Clear deprecation path** — old versions can be sunset with advance notice

### Version Lifecycle

| Phase | Description |
|-------|-------------|
| **Current** | Active development, all features available |
| **Deprecated** | Still functional, no new features, migration guide provided |
| **Sunset** | Removed after 6-month deprecation period |

### Migration Guide

When a new API version is released:
1. Migration guide will be published in this README
2. Deprecated version will be marked in Swagger UI
3. Sunset date will be announced at least 6 months in advance

---

## License

MIT
