# Contributing

Thanks for improving MXXSSM.

## Development setup

```bash
pnpm install
pnpm dev
```

- backend: <http://localhost:8081>
- frontend: <http://localhost:5173>
- Swagger UI: <http://localhost:8081/swagger-ui.html>

The backend needs MySQL 8. See [README.md](README.md#quick-start) for credentials
and environment variables.

## Before opening a pull request

Run the same checks as CI:

```bash
pnpm lint
pnpm test
pnpm build
```

## Coding guidelines

### Backend

- Keep application code inside a feature module under `com.mxxssm.modules`.
- Controllers expose HTTP and validation only; business rules belong in services.
- Services own transactions (`@Transactional`) and repository access.
- Add `@PreAuthorize` on every administration endpoint.
- Add `@AuditLog(action = "...")` to mutating administration endpoints.
- Return DTO records, not entities.
- Throw `BusinessException` with an `ErrorCode`; never build error JSON manually.
- Add Flyway migrations instead of editing existing ones.
- Add tests for authorization, validation and failure paths.

### Frontend

- Use the typed API modules in `src/api`; do not import `axios` in views.
- Add route permission metadata (`meta.permission`) for every protected page.
- Add labels to both `zh-CN` and `en-US` dictionaries.
- Prefer components in `src/components` for repeated UI patterns.
- Keep dialogs `append-to-body` so overlays are not clipped by page transforms.
- Add a Vitest test when adding non-trivial pure logic.

### Database

- Migration names: `V<n>__snake_case_description.sql`.
- Keep SQL portable where possible; the test profile runs H2 in MySQL mode.
- Add indexes for new filter columns used by list endpoints.
- Seed permissions, menu entries and role assignments carefully: an applied
  migration must remain immutable.

## Adding a permission-protected feature

1. Add the permission code in a new Flyway migration.
2. Assign it to the `ADMIN` role (and any other role that needs it).
3. Add a `sys_menu` row with the same `permission_code` if it needs a sidebar entry.
4. Protect the controller method:

   ```java
   @PreAuthorize("hasAuthority('admin:example:read')")
   ```

5. Add the route and `meta.permission` on the frontend.
6. Add an integration test that logs in as a user with/without the permission.

## Commit messages

Use short, imperative messages, optionally conventional prefixes:

```text
feat: add API token management
fix: prevent duplicate role codes
docs: describe CSRF flow
test: cover last-admin protection
```

## Reporting issues

Include:

- steps to reproduce;
- expected vs actual behavior;
- backend log excerpt with the `X-Request-Id`;
- browser console output for UI issues;
- versions of JDK, Maven, Node, pnpm and MySQL.
