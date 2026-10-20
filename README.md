# MXXSSM

**A modern, single-repository full-stack administration foundation.**

MXXSSM started life as a legacy Spring MVC + MyBatis + JSP learning project. This
repository is a clean rewrite as a **modular monolith**: a Spring Boot API and a
Vue 3 single-page application, built and deployed as one artifact.

[English](README.md) · [中文](README.zh-CN.md)

---

## Highlights

- **One deployable artifact** — the SPA is bundled into the Spring Boot jar and served by the same process.
- **Modern backend** — Java 21, Spring Boot 3.5, Spring Security 6, Spring Data JPA, Flyway, MySQL 8, OpenAPI.
- **Modern frontend** — Vue 3.5, Vite 7, TypeScript 5.9, Pinia, Vue Router, Element Plus, vue-i18n.
- **Security by default** — session authentication, BCrypt (cost 12), CSRF protection, method-level RBAC, account status checks.
- **Dynamic RBAC** — users ↔ roles ↔ permissions, database-driven sidebar menus, and authorization checks at the API boundary.
- **Auditability** — login attempts and annotated administration actions are recorded with actor, IP, duration and outcome.
- **Bilingual UI** — complete `zh-CN` / `en-US` dictionaries and server-side localized error messages.
- **Developer experience** — one command for dev, one for build, Flyway migrations, H2 integration tests, ArchUnit rules, ESLint, Prettier, Vitest, GitHub Actions, Docker Compose.

---

## Architecture at a glance

```text
┌──────────────────────────────┐        ┌──────────────────────────────────────────┐
│  Vue 3 SPA (Vite dev server) │        │  Spring Boot monolith (:8081)            │
│                              │        │                                          │
│  Router / Pinia / i18n       │  /api  │  Spring Security (session + CSRF)        │
│  Axios API layer             │ ─────► │      │                                   │
│  Element Plus UI             │        │  REST Controllers (@PreAuthorize)        │
└──────────────────────────────┘        │      │                                   │
                                        │  Services (transactions)                  │
                                        │      │                                   │
                                        │  Spring Data JPA ──► MySQL 8 (Flyway)    │
                                        │      │                                   │
                                        │  Audit AOP ──► login / operation logs    │
                                        └──────────────────────────────────────────┘
```

In production, `frontend/dist` is copied into the Spring Boot jar. One process,
one port, no separate web server required.

```text
MXXSSM/
├── backend/                  Spring Boot application, Flyway migrations, tests
│   └── src/main/java/com/mxxssm/
│       ├── common/           API envelope, errors, security, audit, web filters
│       ├── config/           OpenAPI, MVC and application properties
│       └── modules/          auth, user, role, permission, menu, config, log, dashboard
├── frontend/                 Vue 3 SPA, Element Plus, i18n, unit tests
├── docs/                     Architecture and API documentation
├── .github/workflows/        CI pipeline
├── Dockerfile                Multi-stage Node + Maven + JRE image
├── docker-compose.yml        MySQL 8.4 + application
├── pom.xml                   Maven parent
└── package.json              Root developer scripts
```

---

## Quick start

### Prerequisites

| Tool | Version |
| --- | --- |
| JDK | 21 or newer |
| Maven | 3.9+ |
| Node.js | 22.12+ |
| pnpm | 10+ (repo pins 12.5.1) |
| MySQL | 8.0+ |

### 1. Prepare MySQL

The default development profile connects to:

```text
jdbc:mysql://localhost:3306/mxxssm_admin
username: root
password: 123456
```

The JDBC URL uses `createDatabaseIfNotExist=true`, so a local `root` account can
create the schema automatically. Override credentials with environment variables:

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-password"
```

```bash
export DB_USERNAME=root
export DB_PASSWORD=your-password
```

Flyway creates the tables and seed data on first start. The default administrator is:

```text
username: admin
password: Admin@123
```

> Change this password after the first login. Passwords are stored as BCrypt hashes.

### 2. Run in development

```bash
pnpm install
pnpm dev
```

| Service | URL |
| --- | --- |
| SPA (Vite, hot reload) | http://localhost:5173 |
| API | http://localhost:8081 |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| Health | http://localhost:8081/actuator/health |

Vite proxies `/api` to the backend, so the browser talks to one origin.

### 3. Build for production

```bash
pnpm build
java -jar backend/target/mxxssm.jar --spring.profiles.active=prod
```

> The `prod` profile marks the session cookie as `Secure`. If you are testing
> over plain HTTP locally, add `--server.servlet.session.cookie.secure=false`
> (Docker Compose already does this). Keep the secure cookie enabled behind TLS.

`pnpm build` builds the SPA first and then the Spring Boot jar. The Maven
`with-frontend` profile automatically copies `frontend/dist` into the jar when it
exists.

Production configuration is environment-driven:

| Variable | Purpose |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Set to `prod` |
| `DB_URL` | JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | Database credentials |
| `DB_POOL_SIZE` | Hikari maximum pool size (default 20) |
| `LOG_FILE` | Log path (default `/app/logs/mxxssm.log`) |
| `SPRINGDOC_ENABLED` | Enable Swagger in production (default `false`) |

### 4. Docker Compose

```bash
cp .env.example .env
# edit .env and set strong passwords
docker compose up --build -d
```

The app is available at **http://localhost:8081**. MySQL is exposed on host port
`3307` to avoid clashing with a local MySQL on `3306`.

---

## Feature map

| Area | Capabilities |
| --- | --- |
| Authentication | Login, logout, captcha, public registration, current-user endpoint, session fixation protection |
| Users | Search/filter, pagination, create/edit/delete, role assignment, status management, last-admin protection |
| Roles | CRUD, built-in role protection, user counts, permission assignment |
| Permissions | CRUD, usage protection (roles/menus), code validation |
| Menus | CRUD, sidebar grouping, icon, sort order, permission requirement |
| Audit | Login logs (success/failure, IP, user agent), operation logs (action, path, duration, outcome) |
| Configuration | Runtime key/value settings, used by captcha and registration switches |
| Dashboard | User/role/permission counts, today's login and operation metrics, recent sign-ins |
| Personal area | Profile editing, password change, language and theme preferences |

---

## API overview

All endpoints live under `/api/v1`. Successful responses use:

```json
{
  "success": true,
  "code": "OK",
  "message": "OK",
  "data": {}
}
```

Errors use proper HTTP status codes with the same envelope:

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "Please correct the highlighted fields.",
  "data": [{ "field": "password", "message": "Password must be 8-64 characters." }]
}
```

Paged endpoints return:

```json
{
  "items": [],
  "total": 0,
  "page": 1,
  "size": 10,
  "totalPages": 0
}
```

Key routes:

| Method | Path | Permission |
| --- | --- | --- |
| `POST` | `/api/v1/auth/login` | public |
| `POST` | `/api/v1/auth/logout` | authenticated |
| `GET` | `/api/v1/auth/me` | authenticated |
| `POST` | `/api/v1/auth/register` | public |
| `GET` | `/api/v1/auth/captcha` | public |
| `GET` | `/api/v1/auth/csrf` | public |
| `PUT` | `/api/v1/auth/profile` | `user:profile:update` |
| `PUT` | `/api/v1/auth/password` | `user:password:update` |
| `GET/POST` | `/api/v1/admin/users` | `admin:user:read` / `admin:user:create` |
| `PUT/DELETE` | `/api/v1/admin/users/{id}` | `admin:user:update` / `admin:user:delete` |
| `GET/POST` | `/api/v1/admin/roles` | `admin:role:read` / `admin:role:create` |
| `GET/POST` | `/api/v1/admin/menus` | `admin:menu:read` / `admin:menu:create` |
| `GET/POST` | `/api/v1/admin/permissions` | `admin:permission:read` / `admin:permission:create` |
| `GET` | `/api/v1/admin/logs/login` | `admin:log:login:read` |
| `GET` | `/api/v1/admin/logs/operation` | `admin:log:operation:read` |
| `GET/POST` | `/api/v1/admin/configs` | `admin:config:read` / `admin:config:create` |
| `GET` | `/api/v1/dashboard` | `admin:dashboard:read` |

See [docs/api.md](docs/api.md) for the full table and request examples.

### CSRF for API clients

The SPA uses the double-submit cookie pattern:

1. `GET /api/v1/auth/csrf` sets the `XSRF-TOKEN` cookie.
2. Send the cookie value in the `X-XSRF-TOKEN` header on unsafe requests.
3. Send cookies with `withCredentials` / `credentials: "include"`.
4. The API retries once automatically after refreshing an expired token.

---

## Security model

- **Session authentication** — an HTTP-only `MXXSSM_SESSION` cookie; no JWT storage in the browser.
- **CSRF** — `CookieCsrfTokenRepository` with a SPA-aware request handler. Logout invalidates the session and a new token is issued.
- **Password hashing** — BCrypt with cost 12.
- **RBAC** — permission codes are granted as Spring Security authorities through active roles. Controllers use `@PreAuthorize("hasAuthority('...')")`.
- **Menus** — returned only when the current user holds the menu's permission code.
- **Dashboard data** — global metrics require `admin:dashboard:read`; standard users see a personal shortcut panel instead.
- **Account status** — disabled accounts cannot log in; disabling/deleting the last active administrator is blocked.
- **Audit** — login failures are recorded even when authentication fails; `@AuditLog` actions use `REQUIRES_NEW` so an audit row survives a failed business transaction.
- **Session staleness** — role/permission changes take effect on the next login for existing sessions. This is an intentional trade-off for a small monolith; add a permission cache/version if you need instant revocation.

---

## Database and migrations

- Flyway owns the schema. Hibernate runs with `ddl-auto=validate`.
- Migrations: `backend/src/main/resources/db/migration/V1__init.sql`, `V2__seed.sql`.
- Tables: `sys_user`, `sys_role`, `sys_permission`, `sys_menu`, `sys_user_role`, `sys_role_permission`, `sys_login_log`, `sys_operation_log`, `sys_config`.
- The seed creates roles `ADMIN`/`USER`, 29 permissions, 9 menus, 4 config entries and the `admin` account.

To reset a local database:

```sql
DROP DATABASE mxxssm_admin;
```

Then restart the application; Flyway recreates everything.

---

## Development scripts

| Command | Description |
| --- | --- |
| `pnpm dev` | Run API + Vite dev server together |
| `pnpm dev:api` | Run only Spring Boot |
| `pnpm dev:web` | Run only Vite |
| `pnpm build` | Build SPA and the single executable jar |
| `pnpm test` | Backend + frontend tests |
| `pnpm test:api` | Backend tests (H2 + MockMvc + ArchUnit) |
| `pnpm test:web` | Vitest unit tests |
| `pnpm lint` | ESLint |
| `pnpm format` | Prettier |

---

## Testing

- **Backend** — 14 tests: full login/CSRF/RBAC/user-CRUD flow against H2 with Flyway, plus ArchUnit architecture rules.
- **Frontend** — 9 Vitest unit tests for access checks, menu grouping and formatting.

```bash
pnpm test
```

The backend test profile (`application-test.yml`) uses in-memory H2 in MySQL mode,
so Docker is not required for tests.

---

## Repository conventions

- Backend code is organized by **feature module**, not by layer.
- Controllers do not talk to repositories; services own transactions and JPA access.
- DTOs are Java records; entities stay inside the module.
- All API errors pass through `GlobalExceptionHandler`.
- Frontend uses typed API modules and a single Axios instance; views do not call `axios` directly.
- Permission changes are made in the database/UI and reflected in menus after the next login.

More detail: [docs/architecture.md](docs/architecture.md).

---

## Migration from the legacy SSM version

| Legacy | Now |
| --- | --- |
| Spring MVC XML + `web.xml` | Spring Boot auto-configuration |
| WAR on Tomcat 8.5/9 | Executable jar with embedded Tomcat 10.1 |
| JSP + JSTL + jQuery | Vue 3 SPA + Element Plus |
| MyBatis XML mappers | Spring Data JPA + specifications |
| Manual token cookie + interceptor | Spring Security session + CSRF + method security |
| `applicationContext.xml` data source | `application.yml` + HikariCP |
| Hand-written SQL schema | Flyway versioned migrations |
| Custom JSON utilities | Jackson + typed response envelope |
| Manual page/table JavaScript | Typed API modules + Element Plus tables |

The legacy source is preserved in git history.

---

## Troubleshooting

| Symptom | Fix |
| --- | --- |
| `Port 8081 already in use` | Change `server.port` or stop the other process |
| `Access denied` from MySQL | Set `DB_USERNAME`/`DB_PASSWORD`; ensure the user can create the database |
| Frontend login says CSRF invalid | Open the app through one origin (Vite proxy or the jar), not by mixing ports |
| Flyway validation error | Do not edit applied migration files; add a new `V3__...sql` migration |
| Swagger missing in production | Set `SPRINGDOC_ENABLED=true` (dev is enabled by default) |
| Tests fail on H2 | Run `pnpm test:api`; the test profile uses a separate in-memory database |

---

## License

[MIT](LICENSE)
