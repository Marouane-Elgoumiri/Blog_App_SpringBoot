        # Blog App — Sprint Plan

> Backend: Spring Boot 3.2.4 | Java 17 | PostgreSQL | JWT Auth | Next.js SSR Frontend (future)

---

## Architecture

```
com.example.blog_app_springboot/
├── config/              # SecurityConfig, CorsConfig, OpenApiConfig, JpaConfig, CacheConfig, RateLimitingFilter, AppProperties
├── common/
│   ├── base/            # BaseEntity (id, createdAt, updatedAt)
│   ├── exceptions/      # GlobalExceptionHandler, custom exceptions
│   ├── dtos/            # ErrorResponse, ApiResponse<T>, PageResponse<T>
│   ├── utils/           # SlugGenerator, ReadingTimeCalculator
│   └── constants/       # AppConstants, SecurityConstants
├── security/
│   ├── jwt/             # JwtTokenProvider, JwtAuthFilter
│   ├── config/          # SecurityFilterChain, CustomAuthenticationEntryPoint, CustomAccessDeniedHandler
│   ├── auth/            # AuthService, AuthController, AuthResponse
│   └── util/            # SecurityUtil
├── users/
│   ├── entity/          # UserEntity, Role enum
│   ├── repository/      # UserRepository
│   ├── service/         # UserService
│   ├── controller/      # UsersController
│   └── dtos/            # CreateUserRequest, UserResponse, UserStatsResponse
├── articles/            # (same structure)
├── comments/            # (same structure)
├── tags/                # (same structure)
├── interactions/        # Likes, Bookmarks
├── follows/             # Follow system
├── feed/                # FeedService, FeedController
├── search/              # ArticleSpecifications, SearchService, SearchStrategy implementations
├── analytics/           # ArticleViewEntity, ArticleViewRepository, ViewCountService
└── notifications/       # EmailService, NotificationListener, CommentCreatedEvent, UserFollowedEvent
```

## Design Patterns

| Pattern | Where | Why |
|---------|-------|-----|
| **Repository** | All `*Repository` interfaces | Data access abstraction (Spring Data JPA) |
| **Service Layer** | All `*Service` interfaces + impls | Business logic separation, testable |
| **DTO Pattern** | All `*Request` / `*Response` | Decouple API contracts from entities |
| **Strategy** | `SearchStrategy` (PostgreSQL tsvector vs LIKE fallback) | Swappable search algorithms |
| **Observer** | Spring `@EventListener` for domain events | Decoupled notifications (comment → email, follow → email) |
| **Specification** | `ArticleSpecifications` for search | Dynamic, composable query building |
| **Builder** | Lombok `@Builder` / `@SuperBuilder` on entities/DTOs | Clean object construction |
| **Decorator** | Rate limiting filter (Bucket4j) | Non-invasive request throttling |

## Security Architecture

- **JWT-based stateless auth** — access token (15min) + refresh token (7 days)
- **BCrypt** password hashing
- **Role-based access** — `ROLE_USER`, `ROLE_ADMIN`
- **Method-level security** — `@PreAuthorize` on sensitive endpoints
- **CORS** configured for Next.js frontend origin
- **Input validation** — `@Valid` + Bean Validation on all DTOs
- **SQL injection prevention** — parameterized queries via JPA
- **Rate limiting** — per-IP and per-user throttling

## API Contract

```
POST   /api/v1/auth/login              → { accessToken, refreshToken, user }
POST   /api/v1/auth/refresh            → { accessToken, refreshToken }
POST   /api/v1/auth/logout             → void (blacklists current token)
POST   /api/v1/users                   → { user, token }
GET    /api/v1/users/{id}              → { user }
GET    /api/v1/users/me                → { user } (authenticated)
GET    /api/v1/users/me/stats          → { articles, views, likes, followers } (authenticated)
GET    /api/v1/articles                → PageResponse<ArticleResponse>
GET    /api/v1/articles/{slug}         → ArticleResponse (with author, tags, likes, comments count)
POST   /api/v1/articles                → ArticleResponse (authenticated, creates as draft)
PUT    /api/v1/articles/{id}           → ArticleResponse (owner only)
DELETE /api/v1/articles/{id}           → void (owner only)
GET    /api/v1/articles/my             → PageResponse<ArticleResponse> (incl. drafts, authenticated)
GET    /api/v1/articles/search         → PageResponse<ArticleResponse>
GET    /api/v1/articles/popular        → List<ArticleResponse>
POST   /api/v1/articles/{slug}/comments → CommentResponse (authenticated)
GET    /api/v1/articles/{slug}/comments → List<CommentResponse> (nested)
GET    /api/v1/articles/{slug}/comments/{commentId}/replies → List<CommentResponse>
DELETE /api/v1/articles/{slug}/comments/{commentId} → void (owner or admin, authenticated)
POST   /api/v1/articles/{slug}/like    → { liked: boolean, count } (authenticated)
POST   /api/v1/articles/{slug}/bookmark → { bookmarked: boolean } (authenticated)
POST   /api/v1/users/{id}/follow       → { following: boolean, count } (authenticated)
GET    /api/v1/tags                    → List<TagResponse>
GET    /api/v1/tags/{slug}             → TagResponse
POST   /api/v1/tags                    → TagResponse (admin only)
DELETE /api/v1/tags/{id}               → void (admin only)
GET    /api/v1/feed                    → PageResponse<ArticleResponse> (authenticated)
```

---

## Sprint 0: Foundation & Architecture

**Goal:** Restructure project, establish conventions, add configs.

| # | Task | Details | Status |
|---|------|---------|--------|
| 0.1 | Restructure packages | Move to clean module structure (`entity/`, `repository/`, `service/`, `controller/`, `dtos/` per module) | Completed |
| 0.2 | Add `application.yml` | Multi-profile config: `dev` (H2), `prod` (PostgreSQL), `test` (H2 in-memory) | Completed |
| 0.3 | Create `BaseEntity` | Abstract base with `id`, `createdAt`, `updatedAt`, `@EntityListeners(AuditingEntityListener)` | Completed |
| 0.4 | Create `ApiResponse<T>` and `PageResponse<T>` | Standardized response wrappers for all endpoints | Completed |
| 0.5 | Create `GlobalExceptionHandler` | `@ControllerAdvice` with `@ExceptionHandler` for all exception types | Completed |
| 0.6 | Add OpenAPI/Swagger config | Grouped by module, security scheme for JWT | Completed |
| 0.7 | Add `CorsConfig` | Allow Next.js dev server origins (`localhost:3000`) | Completed |
| 0.8 | Add `JpaConfig` | Enable auditing, configure naming strategy | Completed |
| 0.9 | Update `build.gradle` | Add `jjwt`, `validation`, `mail`, `cache`, `actuator`, `bucket4j` deps | Completed |

---

## Sprint 1: Authentication & Security

**Goal:** Secure the app end-to-end with JWT auth.

| # | Task | Details | Status |
|---|------|---------|--------|
| 1.1 | Add `Role` enum | `USER`, `ADMIN` with `@Enumerated` on `UserEntity` | Completed |
| 1.2 | Update `UserEntity` | Add `roles` (`@ElementCollection`), `email` unique constraint, extend `BaseEntity` | Completed |
| 1.3 | Create `JwtTokenProvider` | Generate/validate access + refresh tokens, extract claims | Completed |
| 1.4 | Create `JwtAuthFilter` | `OncePerRequestFilter`, parse `Authorization: Bearer <token>`, set `SecurityContext` | Completed |
| 1.5 | Create `SecurityConfig` | `SecurityFilterChain`: permit `POST /users/**`, `POST /auth/**`, `GET /swagger-ui/**`; protect all else | Completed |
| 1.6 | Create `AuthService` + `AuthController` | `login()`, `refreshToken()`, `logout()` (token blacklist) | Completed |
| 1.7 | Update `UsersController.signup()` | Hash password with BCrypt before save, return JWT tokens | Completed |
| 1.8 | Fix `UserResponse` | Remove `password` field, add `roles`, `token` | Completed |
| 1.9 | Add `@PreAuthorize` annotations | On all protected endpoints | Completed |
| 1.10 | Write security tests | Test auth flow, unauthorized access, role-based access | Completed |

---

## Sprint 2: Article CRUD Complete

**Goal:** Full article lifecycle with pagination, sorting, and authorization.

| # | Task | Details | Status |
|---|------|---------|--------|
| 2.1 | Update `ArticleEntity` | Extend `BaseEntity`, add `status` enum (`DRAFT`, `PUBLISHED`), indexes | Completed |
| 2.2 | Create `ArticleService` interface + impl | Separate interface for testability | Completed |
| 2.3 | Create `ArticleDtoMapper` | Map `ArticleEntity` → `ArticleResponse`, handle author info, reading time | Completed |
| 2.4 | Wire `ArticlesController` | `POST`, `GET` (paginated, published only), `GET/{slug}`, `PUT/{id}`, `DELETE/{id}`, `GET/my` | Completed |
| 2.5 | Implement proper slug | `SlugGenerator` utility: lowercase, strip special chars, hyphenate, append counter for duplicates | Completed |
| 2.6 | Add pagination | `Pageable` on list endpoint, default `page=0&size=10&sort=createdAt,desc` | Completed |
| 2.7 | Add authorization checks | Only author can edit/delete, admins can do anything | Completed |
| 2.8 | Add `@Valid` | On all `@RequestBody` parameters | Completed |
| 2.9 | Write article tests | Unit + integration tests for all endpoints | Completed |

---

## Sprint 3: Comment System

**Goal:** Full comment CRUD with nested replies.

| # | Task | Details | Status |
|---|------|---------|--------|
| 3.1 | Update `CommentEntity` | Extend `BaseEntity`, add `parentId` (self-referencing `@ManyToOne` for replies), indexes | Completed |
| 3.2 | Create `CommentService` interface + impl | `createComment()`, `getCommentsByArticle()`, `getReplies()`, `deleteComment()` | Completed |
| 3.3 | Create `CommentDtoMapper` | Map to nested response with `replies: List<CommentResponse>` | Completed |
| 3.4 | Wire `CommentController` | `POST /articles/{slug}/comments`, `GET /articles/{slug}/comments`, `GET /articles/{slug}/comments/{commentId}/replies`, `DELETE /articles/{slug}/comments/{commentId}` | Completed |
| 3.5 | Add authorization | Only comment author or admin can delete | Completed |
| 3.6 | Add validation | Body not blank, max length | Completed |
| 3.7 | Write comment tests | Unit + integration tests | Completed |

---

## Sprint 4: Tags System

**Goal:** Tag articles, filter by tags.

| # | Task | Details | Status |
|---|------|---------|--------|
| 4.1 | Create `TagEntity` | `id`, `name` (unique, lowercase), `slug` | Completed |
| 4.2 | Create `@ManyToMany` join | `ArticleEntity` ↔ `TagEntity` with `@JoinTable` | Completed |
| 4.3 | Create `TagService` + `TagController` | `GET /tags` (all tags), `POST /tags` (admin), `DELETE /tags/{id}` (admin) | Completed |
| 4.4 | Update article endpoints | Accept `tags: List<String>` in `CreateArticleRequest`, tags in response | Completed |
| 4.5 | Write tag tests | Unit tests for TagService | Completed |

---

## Sprint 5: Social Interactions

**Goal:** Likes, bookmarks, and follow system.

| # | Task | Details | Status |
|---|------|---------|--------|
| 5.1 | Create `LikeEntity` | Composite key (`userId` + `articleId`), unique constraint | Completed |
| 5.2 | Create `BookmarkEntity` | Composite key (`userId` + `articleId`) | Completed |
| 5.3 | Create `FollowEntity` | Composite key (`followerId` + `followingId`), prevent self-follow | Completed |
| 5.4 | Create `InteractionService` | `toggleLike()`, `toggleBookmark()`, `toggleFollow()` | Completed |
| 5.5 | Wire controllers | `POST /articles/{slug}/like`, `POST /articles/{slug}/bookmark`, `POST /users/{id}/follow` | Completed |
| 5.6 | Add counts to responses | `likeCount`, `bookmarkCount`, `followerCount`, `followingCount` in responses | Completed |
| 5.7 | Add "hasLiked"/"hasBookmarked" | Boolean flags in article response for current user | Completed |
| 5.8 | Write interaction tests | Unit + integration tests | Completed |

---

## Sprint 6: Feed & Search

**Goal:** Personalized feed and full-text search.

| # | Task | Details | Status |
|---|------|---------|--------|
| 6.1 | Create `FeedService` | `getFeed(userId, pageable)` — articles from followed users + popular articles, sorted by date | Completed |
| 6.2 | Wire `FeedController` | `GET /feed` (paginated), `GET /articles/popular` | Completed |
| 6.3 | Create `ArticleSpecification` | JPA `Specification<ArticleEntity>` for dynamic queries | Completed |
| 6.4 | Create `SearchService` | `search(query, tag, author, status, pageable)` using Specifications | Completed |
| 6.5 | Wire search endpoint | `GET /articles/search?q=&tag=&author=&status=` | Completed |
| 6.6 | Add full-text search | PostgreSQL `tsvector` native query with LIKE fallback for H2/dev | Completed |
| 6.7 | Write feed/search tests | Unit + integration tests | Completed |

---

## Sprint 7: Advanced Features

**Goal:** Drafts, reading time, analytics, notifications.

| # | Task | Details | Status |
|---|------|---------|--------|
| 7.1 | Draft/Published logic | `GET /articles` only returns `PUBLISHED`, `GET /articles/my` includes `DRAFT`, author-only draft visibility | Completed |
| 7.2 | Reading time | `ReadingTimeCalculator` utility: word count / 200 wpm, add to `ArticleResponse` | Completed |
| 7.3 | View count tracking | `ArticleViewEntity` with IP-based dedup (24h window), `GET /articles/{slug}` increments, `viewCount` in response, `GET /articles/popular` sorted by views | Completed |
| 7.4 | Spring Events for notifications | `CommentCreatedEvent`, `UserFollowedEvent` with `@EventListener` triggers email | Completed |
| 7.5 | Email service | `JavaMailSender`, async `@Async` email dispatch, "new comment" and "new follower" notifications wired to article author/followed user | Completed |
| 7.6 | Author dashboard | `GET /users/me/stats` — total articles, published, drafts, comments, followers, following | Completed |
| 7.7 | Write tests | Unit + integration tests | Completed |

---

## Sprint 8: Production Readiness

**Goal:** Performance, security hardening, deployment prep.

| # | Task | Details | Status |
|---|------|---------|--------|
| 8.1 | Rate limiting | Bucket4j filter: 100 req/min for authenticated, 20 req/min for anonymous | Completed |
| 8.2 | Caching | `@EnableCaching`, Caffeine cache for tags/userProfiles/articleBySlug/popularArticles | Completed |
| 8.3 | Database indexing | Add `@Index` on frequently queried columns: `slug`, `status`, `authorId`, `createdAt` | Completed |
| 8.4 | Request logging | Custom `RequestLoggingFilter` with method, URI, status, duration, IP, UA | Completed |
| 8.5 | Health checks | `Spring Boot Actuator`: `/actuator/health`, `/actuator/info`, `/actuator/metrics` | Completed |
| 8.6 | API versioning | `/api/v1/` prefix on all endpoints | Completed |
| 8.7 | Docker support | `Dockerfile` multi-stage build, `docker-compose.yml` with PostgreSQL | Completed |
| 8.8 | CI/CD prep | GitHub Actions workflow: build, test, artifact on failure | Completed |
| 8.9 | Security audit | CSRF disabled (stateless), CORS locked down, security headers (HSTS, CSP, Referrer, Permissions), request size limits | Completed |
| 8.10 | Final integration tests | End-to-end test suite covering full user journey (50 API steps verified) | Completed |

---

## Sprint 9: Code Quality & Production Hardening

**Goal:** Environment validation, analytics, documentation, and code quality improvements.

| # | Task | Details | Status |
|---|------|---------|--------|
| 9.1 | Environment variable template | `.env.example` with all required/optional variables documented | Completed |
| 9.2 | Docker HEALTHCHECK | Add `HEALTHCHECK` instruction using `/actuator/health` endpoint | Completed |
| 9.3 | API endpoint reconciliation | Fix `Sprints.md` API contract to match actual implementation (add `GET /users/me`, fix comment delete path) | Completed |
| 9.4 | API versioning documentation | Add versioning strategy section to `README.md` with lifecycle and migration guide | Completed |
| 9.5 | Environment validation | `AppProperties` with `@PostConstruct` validation — fail fast on missing prod variables | Completed |
| 9.6 | Constants package | Create `common/constants/` with `AppConstants` and `SecurityConstants`, refactor `SecurityConfig` and `CacheConfig` | Completed |
| 9.7 | Analytics module | `ArticleViewEntity`, `ArticleViewRepository`, `ViewCountService` with IP-based dedup (24h window), `viewCount` in `ArticleResponse` | Completed |
| 9.8 | Complete email notifications | Wire `NotificationListener` to send actual emails via `EmailService` on comment/follow events | Completed |
| 9.9 | Fix FeedService N+1 query | Add `findByFollowerId()` to `FollowRepository`, replace inefficient stream filtering | Completed |
| 9.10 | API testing guide updates | Add Phase 8.5 (Analytics & View Tracking) to `API_testing.md`, reset progress markers | Completed |
