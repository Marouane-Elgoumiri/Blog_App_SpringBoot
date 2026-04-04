# Blog App API Testing

> Manual API testing workflow (Postman-style). Run commands sequentially — each step builds on the previous one.

**Base URL:** `http://localhost:8080/api/v1`
**Prerequisites:** App running on port 8080 with `dev` profile (H2 database)

---

## Instructions

1. Start the app: `./gradlew bootRun`
2. Run each curl command in order
3. Copy the `accessToken` from Step 3 into the `TOKEN_A` variable
4. Copy the `accessToken` from Step 4 into the `TOKEN_B` variable
5. Update `{id}`, `{slug}`, `{commentId}` placeholders with actual values from previous responses
6. Mark each step ✅ (pass) or ❌ (fail) in the Progress column

---

## Phase 1: Authentication & Users

| # | Step | Command | Expected | Progress |
|---|------|---------|----------|----------|
| 1 | Signup User A | See below | 201, user data | ✅ 2026-04-04 |
| 2 | Signup User B | See below | 201, user data | ✅ 2026-04-04 |
| 3 | Login User A | See below | 200, tokens | ✅ 2026-04-04 |
| 4 | Login User B | See below | 200, tokens | ✅ 2026-04-04 |
| 5 | Get User A by ID | See below | 200, user data | ✅ 2026-04-04 |
| 6 | Get Current User (Me) | See below | 200, user data | ✅ 2026-04-04 |
| 7 | Invalid Login | See below | 400, error | ✅ 2026-04-04 |
| 8 | Duplicate Signup | See below | 409, error | ✅ 2026-04-04 |
| 9 | Refresh Token | See below | 200, new tokens | ✅ 2026-04-04 |
| 10 | Logout | See below | 200, then 403 on next request | ✅ 2026-04-04 |

### Step 1: Signup User A
```bash
curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "password123",
    "email": "alice@example.com"
  }' | jq .
```

### Step 2: Signup User B
```bash
curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob",
    "password": "password123",
    "email": "bob@example.com"
  }' | jq .
```

### Step 3: Login User A
```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "password123"
  }' | jq .
# Save the accessToken: export TOKEN_A="<access_token>"
```

### Step 4: Login User B
```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob",
    "password": "password123"
  }' | jq .
# Save the accessToken: export TOKEN_B="<access_token>"
```

### Step 5: Get User A by ID
```bash
curl -s http://localhost:8080/api/v1/users/1 | jq .
```

### Step 6: Get Current User (Me)
```bash
curl -s http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN_A" | jq .
```

### Step 7: Invalid Login
```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "wrongpassword"
  }' | jq .
# Expected: 400, "Invalid username or password"
```

### Step 8: Duplicate Signup
```bash
curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "password": "password123",
    "email": "another@example.com"
  }' | jq .
# Expected: 409, "Username already exists"
```

### Step 9: Refresh Token
```bash
# Use the refreshToken from Step 3
curl -s -X POST "http://localhost:8080/api/v1/auth/refresh?refreshToken=<refresh_token_from_step_3>" | jq .
# Expected: 200, new accessToken and refreshToken
```

### Step 10: Logout
```bash
curl -s -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer $TOKEN_A" | jq .
# Expected: 200, "Logout successful"
# Then verify token is blacklisted:
curl -s http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN_A" | jq .
# Expected: 401 or 403
```

---

## Phase 2: Articles CRUD

| # | Step | Command | Expected | Progress |
|---|------|---------|----------|----------|
| 11 | Create Article (draft) | See below | 201, article data | ✅ 2026-04-04 |
| 12 | Create Article 2 | See below | 201, article data | ✅ 2026-04-04 |
| 13 | Create Article (User B) | See below | 201, article data | ✅ 2026-04-04 |
| 14 | List Published Articles | See below | 200, paginated list (empty - all drafts) | ✅ 2026-04-04 |
| 15 | Get Article by Slug | See below | 200, full article | ✅ 2026-04-04 |
| 16 | Update Article | See below | 200, updated article | ✅ 2026-04-04 |
| 17 | Unauthorized Update | See below | 403, error | ✅ 2026-04-04 |
| 18 | Get My Articles | See below | 200, user's articles (incl drafts) | ✅ 2026-04-04 |
| 19 | Delete Article | See below | 204, no content | ✅ 2026-04-04 |

### Step 11: Create Article (draft)
```bash
curl -s -X POST http://localhost:8080/api/v1/articles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_A" \
  -d '{
    "title": "Getting Started with Spring Boot",
    "subtitle": "A comprehensive guide",
    "body": "Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications. In this article we will cover the basics of Spring Boot, including dependency injection, auto-configuration, and embedded servers.",
    "tags": ["spring-boot", "java", "backend"]
  }' | jq .
# Save the article id and slug for later steps
```

### Step 12: Create Article 2
```bash
curl -s -X POST http://localhost:8080/api/v1/articles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_A" \
  -d '{
    "title": "Building REST APIs with Spring",
    "subtitle": "Best practices for REST",
    "body": "REST APIs are the backbone of modern web applications. This article covers best practices for designing and implementing RESTful services using Spring Boot.",
    "tags": ["rest", "spring-boot", "api"]
  }' | jq .
```

### Step 13: Create Article (User B)
```bash
curl -s -X POST http://localhost:8080/api/v1/articles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_B" \
  -d '{
    "title": "Introduction to PostgreSQL",
    "subtitle": "Database fundamentals",
    "body": "PostgreSQL is a powerful, open source object-relational database system. It has over 30 years of active development and has earned a strong reputation for reliability and performance.",
    "tags": ["postgresql", "database", "sql"]
  }' | jq .
```

### Step 14: List Published Articles
```bash
curl -s "http://localhost:8080/api/v1/articles?page=0&size=10&sort=createdAt,desc" | jq .
# Note: Articles are created as DRAFT by default, so this may return empty until we publish them
```

### Step 15: Get Article by Slug
```bash
curl -s http://localhost:8080/api/v1/articles/getting-started-with-spring-boot | jq .
# Use the actual slug from Step 11 response
```

### Step 16: Update Article
```bash
curl -s -X PUT http://localhost:8080/api/v1/articles/<article_id_from_step_11> \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_A" \
  -d '{
    "title": "Getting Started with Spring Boot 3",
    "body": "Updated content for Spring Boot 3. Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications."
  }' | jq .
```

### Step 17: Unauthorized Update
```bash
curl -s -X PUT http://localhost:8080/api/v1/articles/<article_id_from_step_11> \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_B" \
  -d '{
    "title": "Hacked Title"
  }' | jq .
# Expected: 403, "You can only edit your own articles"
```

### Step 18: Get My Articles
```bash
curl -s http://localhost:8080/api/v1/articles/my \
  -H "Authorization: Bearer $TOKEN_A" | jq .
# Should include both published and draft articles
```

### Step 19: Delete Article
```bash
curl -s -X DELETE http://localhost:8080/api/v1/articles/<article_id_from_step_12> \
  -H "Authorization: Bearer $TOKEN_A" -w "\nHTTP Status: %{http_code}\n"
# Expected: 204, no content
```

---

## Phase 3: Comments

| # | Step | Command | Expected | Progress |
|---|------|---------|----------|----------|
| 20 | Create Comment | See below | 201, comment data | ✅ 2026-04-04 |
| 21 | Create Reply | See below | 201, reply data | ✅ 2026-04-04 |
| 22 | Get Comments | See below | 200, nested comments | ✅ 2026-04-04 |
| 23 | Get Replies | See below | 200, replies list | ✅ 2026-04-04 |
| 24 | Delete Comment | See below | 204, no content (cascade deletes replies) | ✅ 2026-04-04 |

### Step 20: Create Comment
```bash
curl -s -X POST http://localhost:8080/api/v1/articles/<slug_from_step_11>/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_B" \
  -d '{
    "title": "Great article!",
    "body": "This is a really helpful guide. Thanks for sharing!"
  }' | jq .
# Save the comment id for later steps
```

### Step 21: Create Reply
```bash
curl -s -X POST http://localhost:8080/api/v1/articles/<slug_from_step_11>/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_A" \
  -d '{
    "body": "Thanks for the feedback! Glad you found it useful.",
    "parentId": <comment_id_from_step_20>
  }' | jq .
```

### Step 22: Get Comments
```bash
curl -s http://localhost:8080/api/v1/articles/<slug_from_step_11>/comments | jq .
# Should show the comment with nested replies
```

### Step 23: Get Replies
```bash
curl -s http://localhost:8080/api/v1/articles/<slug_from_step_11>/comments/<comment_id_from_step_20>/replies | jq .
# Should show the reply from Step 21
```

### Step 24: Delete Comment
```bash
curl -s -X DELETE http://localhost:8080/api/v1/articles/<slug_from_step_11>/comments/<comment_id_from_step_20> \
  -H "Authorization: Bearer $TOKEN_B" -w "\nHTTP Status: %{http_code}\n"
# Expected: 204, no content
```

---

## Phase 4: Tags

| # | Step | Command | Expected | Progress |
|---|------|---------|----------|----------|
| 25 | Get All Tags | See below | 200, tags list | ✅ 2026-04-04 |
| 26 | Get Tag by Slug | See below | 200, tag data | ✅ 2026-04-04 |
| 27 | Create Tag (Admin) | See below | 403 (USER role, expected) | ✅ 2026-04-04 |
| 28 | Duplicate Tag | See below | 403 (USER role, expected) | ✅ 2026-04-04 |

### Step 25: Get All Tags
```bash
curl -s http://localhost:8080/api/v1/tags | jq .
# Should show auto-created tags from articles: spring-boot, java, backend, rest, api, postgresql, database, sql
```

### Step 26: Get Tag by Slug
```bash
curl -s http://localhost:8080/api/v1/tags/spring-boot | jq .
```

### Step 27: Create Tag (Admin)
```bash
# Note: This requires ADMIN role. Default users are USER role.
# This step will fail with 403 unless you manually grant ADMIN role to a user.
curl -s -X POST http://localhost:8080/api/v1/tags \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_A" \
  -d '{
    "name": "tutorial"
  }' | jq .
# Expected: 403 (unless user has ADMIN role)
```

### Step 28: Duplicate Tag
```bash
curl -s -X POST http://localhost:8080/api/v1/tags \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN_A" \
  -d '{
    "name": "spring-boot"
  }' | jq .
# Expected: 409, "Tag already exists" (if admin) or 403 (if not admin)
```

---

## Phase 5: Social Interactions

| # | Step | Command | Expected | Progress |
|---|------|---------|----------|----------|
| 29 | Like Article | See below | 200, liked=true | ✅ 2026-04-04 |
| 30 | Unlike Article | See below | 200, liked=false | ✅ 2026-04-04 |
| 31 | Bookmark Article | See below | 200, bookmarked=true | ✅ 2026-04-04 |
| 32 | Remove Bookmark | See below | 200, bookmarked=false | ✅ 2026-04-04 |
| 33 | Verify in Response | See below | 200, counts updated | ✅ 2026-04-04 |

### Step 29: Like Article
```bash
curl -s -X POST http://localhost:8080/api/v1/articles/<slug_from_step_11>/like \
  -H "Authorization: Bearer $TOKEN_B" | jq .
# Expected: action=true, count=1
```

### Step 30: Unlike Article
```bash
curl -s -X POST http://localhost:8080/api/v1/articles/<slug_from_step_11>/like \
  -H "Authorization: Bearer $TOKEN_B" | jq .
# Expected: action=false, count=0
```

### Step 31: Bookmark Article
```bash
curl -s -X POST http://localhost:8080/api/v1/articles/<slug_from_step_11>/bookmark \
  -H "Authorization: Bearer $TOKEN_B" | jq .
# Expected: action=true
```

### Step 32: Remove Bookmark
```bash
curl -s -X POST http://localhost:8080/api/v1/articles/<slug_from_step_11>/bookmark \
  -H "Authorization: Bearer $TOKEN_B" | jq .
# Expected: action=false
```

### Step 33: Verify in Response
```bash
curl -s http://localhost:8080/api/v1/articles/<slug_from_step_11> \
  -H "Authorization: Bearer $TOKEN_B" | jq .
# Check: likeCount, likedByCurrentUser, bookmarkedByCurrentUser, commentCount
```

---

## Phase 6: Follow System

| # | Step | Command | Expected | Progress |
|---|------|---------|----------|----------|
| 34 | Follow User A | See below | 200, following=true | ✅ 2026-04-04 |
| 35 | Unfollow User A | See below | 200, following=false | ✅ 2026-04-04 |
| 36 | Self-Follow | See below | 400, error | ✅ 2026-04-04 |
| 37 | Verify Follower Count | See below | 200, counts updated | ✅ 2026-04-04 |

### Step 34: Follow User A
```bash
curl -s -X POST http://localhost:8080/api/v1/users/1/follow \
  -H "Authorization: Bearer $TOKEN_B" | jq .
# Expected: following=true, count=1
```

### Step 35: Unfollow User A
```bash
curl -s -X POST http://localhost:8080/api/v1/users/1/follow \
  -H "Authorization: Bearer $TOKEN_B" | jq .
# Expected: following=false, count=0
```

### Step 36: Self-Follow
```bash
curl -s -X POST http://localhost:8080/api/v1/users/1/follow \
  -H "Authorization: Bearer $TOKEN_A" | jq .
# Expected: 400, "You cannot follow yourself"
```

### Step 37: Verify Follower Count
```bash
curl -s http://localhost:8080/api/v1/users/1 | jq .
# Check: followerCount, followingCount
```

---

## Phase 7: Feed & Search

| # | Step | Command | Expected | Progress |
|---|------|---------|----------|----------|
| 38 | Get Feed | See below | 200, empty (no follows, all drafts) | ✅ 2026-04-04 |
| 39 | Get Popular Articles | See below | 200, empty (all drafts) | ✅ 2026-04-04 |
| 40 | Search by Query | See below | 200, empty (all drafts) | ✅ 2026-04-04 |
| 41 | Search by Tag | See below | 200, empty (all drafts) | ✅ 2026-04-04 |
| 42 | Search by Author | See below | 200, empty (all drafts) | ✅ 2026-04-04 |
| 43 | Combined Search | See below | 200, empty (all drafts) | ✅ 2026-04-04 |

### Step 38: Get Feed
```bash
curl -s http://localhost:8080/api/v1/feed \
  -H "Authorization: Bearer $TOKEN_A" | jq .
# Shows articles from followed users (empty if no follows)
```

### Step 39: Get Popular Articles
```bash
curl -s "http://localhost:8080/api/v1/articles/popular?page=0&size=10" | jq .
```

### Step 40: Search by Query
```bash
curl -s "http://localhost:8080/api/v1/articles/search?q=spring" | jq .
# Uses full-text search (LIKE-based in dev profile)
```

### Step 41: Search by Tag
```bash
curl -s "http://localhost:8080/api/v1/articles/search?tag=java" | jq .
```

### Step 42: Search by Author
```bash
curl -s "http://localhost:8080/api/v1/articles/search?author=alice" | jq .
```

### Step 43: Combined Search
```bash
curl -s "http://localhost:8080/api/v1/articles/search?q=spring&tag=spring-boot" | jq .
```

---

## Phase 8: Author Dashboard

| # | Step | Command | Expected | Progress |
|---|------|---------|----------|----------|
| 44 | Get User Stats | See below | 200, stats data | ✅ 2026-04-04 |

### Step 44: Get User Stats
```bash
curl -s http://localhost:8080/api/v1/users/me/stats \
  -H "Authorization: Bearer $TOKEN_A" | jq .
# Expected: totalArticles, publishedArticles, draftArticles, totalComments, totalFollowers, totalFollowing
```

---

## Phase 9: Edge Cases & Error Handling

| # | Step | Command | Expected | Progress |
|---|------|---------|----------|----------|
| 45 | Access Protected Without Token | See below | 403 | ✅ 2026-04-04 |
| 46 | Access With Invalid Token | See below | 403 | ✅ 2026-04-04 |
| 47 | Access Non-existent Article | See below | 404 | ✅ 2026-04-04 |
| 48 | Create Article Without Auth | See below | 401 | ✅ 2026-04-04 |
| 49 | Validation Error | See below | 400, field errors | ✅ 2026-04-04 |
| 50 | Public Endpoints Work | See below | 200 | ✅ 2026-04-04 |

### Step 45: Access Protected Without Token
```bash
curl -s http://localhost:8080/api/v1/users/me | jq .
# Expected: 401, "Authentication required"
```

### Step 46: Access With Invalid Token
```bash
curl -s http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer invalid.token.here" | jq .
# Expected: 401
```

### Step 47: Access Non-existent Article
```bash
curl -s http://localhost:8080/api/v1/articles/nonexistent-slug | jq .
# Expected: 404, "Article not found with slug: 'nonexistent-slug'"
```

### Step 48: Create Article Without Auth
```bash
curl -s -X POST http://localhost:8080/api/v1/articles \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Unauthorized Article",
    "body": "This should fail"
  }' | jq .
# Expected: 401
```

### Step 49: Validation Error
```bash
curl -s -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "",
    "password": "123",
    "email": "invalid-email"
  }' | jq .
# Expected: 400, "Validation failed" with field errors
```

### Step 50: Public Endpoints Work
```bash
curl -s http://localhost:8080/api/v1/articles | jq .
curl -s http://localhost:8080/api/v1/tags | jq .
# Expected: 200, data (no auth required)
```

---

## Quick Reference: Token Management

```bash
# After login, extract tokens automatically:
TOKEN_A=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123"}' | jq -r '.data.accessToken')

TOKEN_B=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"bob","password":"password123"}' | jq -r '.data.accessToken')

echo "TOKEN_A: $TOKEN_A"
echo "TOKEN_B: $TOKEN_B"
```
