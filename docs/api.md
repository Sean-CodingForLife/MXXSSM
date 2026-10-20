# API reference

Base path: `/api/v1`

All routes return the envelope below. Errors use the corresponding HTTP status
code and the same shape.

```json
{
  "success": true,
  "code": "OK",
  "message": "OK",
  "data": null
}
```

Paged responses return `data` as:

```json
{
  "items": [],
  "total": 0,
  "page": 1,
  "size": 10,
  "totalPages": 0
}
```

## Authentication

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `GET` | `/auth/captcha` | public | Returns a base64 PNG captcha. The code is stored in the session with a TTL. |
| `GET` | `/auth/csrf` | public | Ensures the `XSRF-TOKEN` cookie is issued and returns the token metadata. |
| `POST` | `/auth/login` | public + CSRF | `{ "username", "password", "captcha"? }` |
| `POST` | `/auth/logout` | authenticated + CSRF | Invalidates the session. |
| `POST` | `/auth/register` | public + CSRF | `{ "username", "displayName", "password", "email"?, "phone"? }` |
| `GET` | `/auth/me` | authenticated | Current user, roles, permissions, menus. |
| `PUT` | `/auth/profile` | `user:profile:update` | `{ "displayName", "email"?, "phone"? }` |
| `PUT` | `/auth/password` | `user:password:update` | `{ "currentPassword", "newPassword" }` |

### Login example

```http
POST /api/v1/auth/login
Content-Type: application/json
X-XSRF-TOKEN: <value of XSRF-TOKEN cookie>
Cookie: XSRF-TOKEN=<value>; MXXSSM_SESSION=<session>

{
  "username": "admin",
  "password": "Admin@123",
  "captcha": "AB3D"
}
```

```json
{
  "success": true,
  "code": "OK",
  "message": "Login succeeded.",
  "data": {
    "id": 1,
    "username": "admin",
    "displayName": "Administrator",
    "email": "admin@mxxssm.local",
    "phone": null,
    "lastLoginAt": "2026-10-19T18:41:00",
    "roles": ["ADMIN"],
    "permissions": ["admin:config:create", "..."],
    "menus": [
      {
        "id": 1,
        "code": "users",
        "name": "Users",
        "path": "/system/users",
        "icon": "User",
        "groupName": "System",
        "permissionCode": "admin:user:read",
        "sortOrder": 10,
        "status": "ACTIVE"
      }
    ]
  }
}
```

## Users

| Method | Path | Permission | Description |
| --- | --- | --- | --- |
| `GET` | `/admin/users?keyword=&status=&role=&page=1&size=10` | `admin:user:read` | Filtered, paged list. |
| `GET` | `/admin/users/{id}` | `admin:user:read` | Single user. |
| `POST` | `/admin/users` | `admin:user:create` | Create an account. |
| `PUT` | `/admin/users/{id}` | `admin:user:update` | Update profile, status and roles. |
| `DELETE` | `/admin/users/{id}` | `admin:user:delete` | Delete an account. |

Create body:

```json
{
  "username": "alice",
  "displayName": "Alice",
  "password": "Alice1234",
  "email": "alice@example.com",
  "phone": "",
  "roles": ["USER"],
  "status": "ACTIVE"
}
```

Update body (no username/password):

```json
{
  "displayName": "Alice Chen",
  "email": "alice@example.com",
  "phone": "13800000000",
  "roles": ["USER"],
  "status": "ACTIVE"
}
```

Business rules:

- usernames are unique and immutable;
- passwords are 8-64 characters and must contain a letter and a digit;
- at least one active `ADMIN` must remain;
- you cannot disable or delete your own account;
- deleting a user removes its role links.

## Roles

| Method | Path | Permission | Description |
| --- | --- | --- | --- |
| `GET` | `/admin/roles?keyword=&page=1&size=10` | `admin:role:read` | Paged list with user counts. |
| `GET` | `/admin/roles/all` | `admin:role:read` | Active roles for selects. |
| `GET` | `/admin/roles/{id}` | `admin:role:read` | Single role. |
| `POST` | `/admin/roles` | `admin:role:create` | `{ "code", "name", "description"?, "status"? }` |
| `PUT` | `/admin/roles/{id}` | `admin:role:update` | `{ "name", "description"?, "status" }` |
| `DELETE` | `/admin/roles/{id}` | `admin:role:delete` | Rejects built-in roles or roles with users. |
| `GET` | `/admin/roles/{id}/permissions` | `admin:role:permission:read` | Assigned permissions. |
| `PUT` | `/admin/roles/{id}/permissions` | `admin:role:permission:update` | `{ "permissionIds": [1, 2] }` |

Built-in role codes `ADMIN` and `USER` cannot be deleted. Built-in roles cannot be
disabled.

## Permissions

| Method | Path | Permission | Description |
| --- | --- | --- | --- |
| `GET` | `/admin/permissions?keyword=&page=1&size=10` | `admin:permission:read` | Paged list. |
| `GET` | `/admin/permissions/all` | `admin:permission:read` | Active permissions for selects. |
| `POST` | `/admin/permissions` | `admin:permission:create` | `{ "code", "name", "description"?, "status"? }` |
| `PUT` | `/admin/permissions/{id}` | `admin:permission:update` | Name/description/status only; code is immutable. |
| `DELETE` | `/admin/permissions/{id}` | `admin:permission:delete` | Rejected while assigned to roles or menus. |

Permission code pattern: `^[a-z][a-z0-9:_-]{2,63}$`.

## Menus

| Method | Path | Permission | Description |
| --- | --- | --- | --- |
| `GET` | `/admin/menus?keyword=&page=1&size=10` | `admin:menu:read` | Paged list. |
| `GET` | `/admin/menus/all` | `admin:menu:read` | All menus. |
| `POST` | `/admin/menus` | `admin:menu:create` | Create a menu. |
| `PUT` | `/admin/menus/{id}` | `admin:menu:update` | Update a menu. |
| `DELETE` | `/admin/menus/{id}` | `admin:menu:delete` | Delete a menu. |

Menu body:

```json
{
  "code": "users",
  "name": "Users",
  "path": "/system/users",
  "icon": "User",
  "groupName": "System",
  "permissionCode": "admin:user:read",
  "sortOrder": 10,
  "status": "ACTIVE"
}
```

The selected permission code must exist.

## System configuration

| Method | Path | Permission | Description |
| --- | --- | --- | --- |
| `GET` | `/admin/configs?keyword=&page=1&size=10` | `admin:config:read` | Paged list. |
| `GET` | `/admin/configs/all` | `admin:config:read` | All entries. |
| `POST` | `/admin/configs` | `admin:config:create` | Create an entry. |
| `PUT` | `/admin/configs/{id}` | `admin:config:update` | Update an entry. |
| `DELETE` | `/admin/configs/{id}` | `admin:config:delete` | Delete an entry. |

Seeded keys:

| Key | Default | Purpose |
| --- | --- | --- |
| `site.name` | `MXXSSM` | Display name. |
| `security.captcha.enabled` | `true` | Login captcha switch. |
| `security.registration.enabled` | `true` | Public registration switch. |
| `ui.default-locale` | `zh-CN` | Default UI locale. |

## Audit logs

| Method | Path | Permission | Description |
| --- | --- | --- | --- |
| `GET` | `/admin/logs/login?keyword=&status=&page=1&size=10` | `admin:log:login:read` | Login attempts. |
| `GET` | `/admin/logs/operation?keyword=&status=&page=1&size=10` | `admin:log:operation:read` | Annotated admin operations. |

Status values are `SUCCESS` and `FAIL`.

## Dashboard and public settings

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `GET` | `/dashboard` | `admin:dashboard:read` | Counts, today's metrics and recent sign-ins. |
| `GET` | `/public/settings` | public | Site name, captcha/registration flags, default locale, version. |
| `GET` | `/actuator/health` | public | Health probe. |
| `GET` | `/actuator/info` | public | Build info. |

## Error codes

| HTTP | Code | Meaning |
| --- | --- | --- |
| 400 | `VALIDATION_ERROR` | Bean validation failed; `data` contains field errors. |
| 400 | `CAPTCHA_INVALID` | Captcha missing/incorrect/expired. |
| 400 | `PASSWORD_MISMATCH` | Current password is wrong. |
| 400 | `PASSWORD_POLICY` | Password policy not met. |
| 400 | `BAD_REQUEST` | Malformed request. |
| 400 | `SELF_OPERATION` | Disabling/deleting your own account. |
| 401 | `AUTH_REQUIRED` | No valid session. |
| 401 | `BAD_CREDENTIALS` | Wrong username/password. |
| 403 | `ACCESS_DENIED` | Missing permission. |
| 403 | `ACCOUNT_DISABLED` | Account disabled. |
| 403 | `CSRF_INVALID` | CSRF token missing/expired. |
| 403 | `REGISTRATION_DISABLED` | Public registration is off. |
| 404 | `USER_NOT_FOUND`, `ROLE_NOT_FOUND`, `PERMISSION_NOT_FOUND`, `MENU_NOT_FOUND`, `CONFIG_NOT_FOUND` | Missing entity. |
| 409 | `USERNAME_EXISTS`, `ROLE_CODE_EXISTS`, `PERMISSION_CODE_EXISTS`, `MENU_CODE_EXISTS`, `CONFIG_KEY_EXISTS` | Unique conflict. |
| 409 | `ROLE_IN_USE`, `PERMISSION_IN_USE`, `ROLE_BUILT_IN`, `LAST_ADMIN` | Business conflict. |
| 503 | `DATABASE_ERROR` | Database unavailable. |
| 500 | `INTERNAL_ERROR` | Unexpected server error. |

Every response carries an `X-Request-Id` header; the same id appears in the
server log via MDC.

## Permission codes

| Code | Scope |
| --- | --- |
| `admin:dashboard:read` | Global dashboard metrics |
| `admin:user:read` / `create` / `update` / `delete` | Users |
| `admin:role:read` / `create` / `update` / `delete` | Roles |
| `admin:role:permission:read` / `update` | Role permission assignment |
| `admin:menu:read` / `create` / `update` / `delete` | Menus |
| `admin:permission:read` / `create` / `update` / `delete` | Permissions |
| `admin:log:login:read` | Login logs |
| `admin:log:operation:read` | Operation logs |
| `admin:config:read` / `create` / `update` / `delete` | System config |
| `user:profile:read` / `update` | Own profile |
| `user:password:update` | Own password |
| `user:settings:read` | Personal settings |
