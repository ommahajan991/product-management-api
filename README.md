# Products API

A RESTful API for managing Products and Items, built with Spring Boot, Spring Security (JWT with refresh token rotation), Spring Data JPA, and PostgreSQL.

## Tech Stack

- Java 17
- Spring Boot 4.1.1
- Spring Security 7.1.1 (JWT + refresh token rotation)
- Spring Data JPA / Hibernate ORM 7.4.5
- PostgreSQL (H2 for tests)
- JUnit 5 + Mockito
- Springdoc OpenAPI (Swagger)
- Docker & Docker Compose

## Architecture

Layered architecture with interface + implementation separation at the service layer:

Controller → Service (interface + impl) → Repository → Entity


- **Controllers** — handle HTTP concerns only (status codes, path/query binding); never touch entities directly.
- **Services** — business logic, transaction boundaries (`@Transactional`), and entity ↔ DTO mapping. Mapping happens inside `@Transactional` service methods to avoid lazy-loading issues with `Product.items`.
- **DTOs** — separate request/response records per resource; entities are never exposed over the API.
- **Security** — stateless JWT access tokens (15 min expiry) + database-backed, rotating refresh tokens (7 day expiry). Uses Spring Security's standard `UserDetails`/`UserDetailsService`/`AuthenticationManager`/`DaoAuthenticationProvider` chain rather than a claims-only filter, so role/account changes take effect immediately rather than waiting for token expiry.
- **Async** — product creation triggers an async audit log write (`@Async`, dedicated thread pool) so audit logging never blocks the API response.

## Setup

### Option A — Docker Compose (recommended)

1. Create a `.env` file in the project root: 
    DB_USERNAME=products_user
   DB_PASSWORD=products_pass
   JWT_SECRET=<generate with: openssl rand -base64 32>

2. 2. Run:
```bash
   docker compose up --build -d
```
3. API available at `http://localhost:8080`.

### Option B — Local (Maven + local PostgreSQL)

1. Have PostgreSQL running locally, with a database named `product_management_api`.
2. Set environment variables (or edit the defaults in `application.yml` directly for local dev):
```bash
   export DB_USERNAME=products_user
   export DB_PASSWORD=products_pass
   export JWT_SECRET=$(openssl rand -base64 32)
```
3. Run:
```bash
   mvn spring-boot:run
```

**Note:** only one of Docker or local Maven can run at a time — both bind to port 8080.

## API Documentation

Once running: `http://localhost:8080/swagger-ui.html`

Click **Authorize** and paste an access token (obtained via `/auth/login`) to test authenticated endpoints directly from the UI.

## Authentication Flow

1. `POST /api/v1/auth/register` — create an account (`username`, `password`)
2. `POST /api/v1/auth/login` — returns `accessToken` (15 min) + `refreshToken` (7 days)
3. Use `Authorization: Bearer <accessToken>` on all `/api/v1/products/**` requests
4. `POST /api/v1/auth/refresh` — exchange a valid refresh token for a new access + refresh token pair. The old refresh token is revoked immediately (rotation) — reusing it afterward returns `401`.
5. `POST /api/v1/auth/logout` — revokes the given refresh token.

## Endpoints

| Method | Path | Auth required |
|---|---|---|
| POST | `/api/v1/auth/register` | No |
| POST | `/api/v1/auth/login` | No |
| POST | `/api/v1/auth/refresh` | No |
| POST | `/api/v1/auth/logout` | No |
| POST | `/api/v1/products` | Yes |
| GET | `/api/v1/products` | Yes |
| GET | `/api/v1/products/{id}` | Yes |
| PUT | `/api/v1/products/{id}` | Yes |
| DELETE | `/api/v1/products/{id}` | Yes |
| GET | `/api/v1/products/{id}/items` | Yes |

List endpoints support pagination via `?page=0&size=20&sort=productName,asc`.

## Running Tests

```bash
mvn test
```

Covers:
- Unit tests (services, mocked repositories) — `ProductServiceImplTest`, `ItemServiceImplTest`, `AuthServiceImplTest`, `JwtServiceTest`
- Controller tests (`@WebMvcTest` + `MockMvc`) — `ProductControllerTest`, `AuthControllerTest`, `ItemControllerTest`
- Integration tests (`@SpringBootTest`, H2 in-memory DB) — full product lifecycle, auth flow, refresh token rotation

## Environment Variables

| Variable | Required | Description |
|---|---|---|
| `DB_USERNAME` | Yes | PostgreSQL username |
| `DB_PASSWORD` | Yes | PostgreSQL password |
| `JWT_SECRET` | Yes | Base64-encoded secret for JWT signing (generate with `openssl rand -base64 32`) |

## Known Limitations / Assumptions

- Access tokens remain valid until natural expiry even after logout — logout only revokes the refresh token (standard behavior for stateless JWTs; a token blocklist would be needed to revoke access tokens immediately).
- Full Item CRUD (create/update/delete) is not implemented — only `GET /products/{id}/items`, matching the endpoint list specified in the assignment.
- HTTPS enforcement is expected to be handled at the reverse-proxy/deployment layer, not within the application itself.
- All registered users default to the `USER` role; there's no built-in admin-provisioning endpoint (would need to be seeded manually or via a future admin endpoint).