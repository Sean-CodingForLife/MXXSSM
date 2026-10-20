# MXXSSM

**现代化的单体全栈后台管理基础仓库。**

MXXSSM 最初是一个基于 Spring MVC + MyBatis + JSP 的传统 SSM 学习项目。本仓库将其彻底重构为
**模块化单体**：Spring Boot 提供 API，Vue 3 提供单页应用，最终打包为一个可执行 Jar。

[English](README.md) · **中文**

---

## 核心特性

- **单产物部署**：前端构建产物打入 Spring Boot Jar，同进程、同端口提供服务。
- **现代后端**：Java 21、Spring Boot 3.5、Spring Security 6、Spring Data JPA、Flyway、MySQL 8、OpenAPI。
- **现代前端**：Vue 3.5、Vite 7、TypeScript 5.9、Pinia、Vue Router、Element Plus、vue-i18n。
- **默认安全**：Session 登录、BCrypt（cost 12）、CSRF 防护、方法级 RBAC、账号状态校验。
- **动态权限**：用户 ↔ 角色 ↔ 权限，数据库驱动侧边栏菜单，接口统一权限校验。
- **完整审计**：登录成功/失败、关键管理操作均记录操作人、IP、耗时与结果。
- **中英双语**：前端完整 `zh-CN` / `en-US` 词典，后端错误信息本地化。
- **工程化**：一键开发/构建、Flyway 迁移、H2 集成测试、ArchUnit、ESLint、Prettier、Vitest、GitHub Actions、Docker Compose。

---

## 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Java 21、Spring Boot 3.5、Spring Security 6、Spring Data JPA、Flyway、MySQL 8 |
| 前端 | Vue 3.5、Vite 7、TypeScript 5.9、Pinia、Vue Router 4、Element Plus、vue-i18n |
| 测试 | JUnit 5、MockMvc、H2、ArchUnit、Vitest |
| 构建/部署 | Maven、pnpm、Docker 多阶段构建、GitHub Actions |

---

## 快速开始

### 环境要求

| 工具 | 版本 |
| --- | --- |
| JDK | 21 或更高 |
| Maven | 3.9+ |
| Node.js | 22.12+ |
| pnpm | 10+（仓库固定 12.5.1） |
| MySQL | 8.0+ |

### 1. 准备数据库

开发环境默认连接：

```text
jdbc:mysql://localhost:3306/mxxssm_admin
用户名：root
密码：123456
```

JDBC URL 带有 `createDatabaseIfNotExist=true`，本地 `root` 账号会自动建库。也可以通过环境变量覆盖：

```powershell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "你的密码"
```

```bash
export DB_USERNAME=root
export DB_PASSWORD=你的密码
```

首次启动时 Flyway 自动建表并写入种子数据。默认管理员：

```text
账号：admin
密码：Admin@123
```

> 首次登录后请立即修改密码。密码以 BCrypt 哈希存储。

### 2. 开发模式

```bash
pnpm install
pnpm dev
```

| 服务 | 地址 |
| --- | --- |
| 前端（Vite 热更新） | http://localhost:5173 |
| 后端 API | http://localhost:8081 |
| Swagger UI | http://localhost:8081/swagger-ui.html |
| 健康检查 | http://localhost:8081/actuator/health |

Vite 将 `/api` 代理到后端，浏览器始终只访问一个源。

### 3. 生产构建

```bash
pnpm build
java -jar backend/target/mxxssm.jar --spring.profiles.active=prod
```

> `prod` Profile 会把 Session Cookie 标记为 `Secure`。如果在本机通过 HTTP 测试，请追加
> `--server.servlet.session.cookie.secure=false`（Docker Compose 已做处理）。生产环境启用 TLS 时应保持安全 Cookie。

`pnpm build` 先构建前端，再打包 Spring Boot Jar。Maven 的 `with-frontend`
Profile 会在 `frontend/dist` 存在时自动复制静态资源。

生产环境变量：

| 变量 | 说明 |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | 设为 `prod` |
| `DB_URL` | JDBC 连接串 |
| `DB_USERNAME` / `DB_PASSWORD` | 数据库账号密码 |
| `DB_POOL_SIZE` | Hikari 最大连接数（默认 20） |
| `LOG_FILE` | 日志路径（默认 `/app/logs/mxxssm.log`） |
| `SPRINGDOC_ENABLED` | 生产环境是否启用 Swagger（默认 `false`） |

### 4. Docker Compose

```bash
cp .env.example .env
# 修改 .env 中的密码
docker compose up --build -d
```

访问 **http://localhost:8081**。MySQL 暴露在宿主机 `3307` 端口，避免与本机 `3306` 冲突。

---

## 功能清单

| 模块 | 能力 |
| --- | --- |
| 认证 | 登录、退出、验证码、公开注册、当前用户、会话固定攻击防护 |
| 用户 | 搜索筛选、分页、新增/编辑/删除、角色分配、状态管理、最后管理员保护 |
| 角色 | 增删改查、内置角色保护、用户数统计、权限分配 |
| 权限 | 增删改查、被引用保护（角色/菜单）、编码校验 |
| 菜单 | 增删改查、分组、图标、排序、权限要求 |
| 审计 | 登录日志（成功/失败、IP、UA）、操作日志（动作、路径、耗时、结果） |
| 配置 | 运行时键值配置，驱动验证码与注册开关 |
| 控制台 | 用户/角色/权限统计、今日登录与操作指标、最近登录 |
| 个人中心 | 资料编辑、修改密码、语言与主题偏好 |

---

## API 约定

所有接口位于 `/api/v1`。成功响应：

```json
{
  "success": true,
  "code": "OK",
  "message": "OK",
  "data": {}
}
```

错误响应使用正确的 HTTP 状态码，结构一致：

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "请检查表单中标红的字段。",
  "data": [{ "field": "password", "message": "密码长度需为 8-64 位。" }]
}
```

分页数据：

```json
{
  "items": [],
  "total": 0,
  "page": 1,
  "size": 10,
  "totalPages": 0
}
```

完整接口列表见 [docs/api.md](docs/api.md)，或启动后访问 Swagger UI。

### CSRF

SPA 使用双提交 Cookie：

1. `GET /api/v1/auth/csrf` 写入 `XSRF-TOKEN` Cookie；
2. 非安全请求将 Cookie 值放入 `X-XSRF-TOKEN` 请求头；
3. 请求需携带 Cookie（`withCredentials` / `credentials: "include"`）；
4. 令牌过期时前端会自动刷新并重试一次。

---

## 安全模型

- **Session 认证**：HTTP-only `MXXSSM_SESSION` Cookie，浏览器不保存 JWT。
- **CSRF**：`CookieCsrfTokenRepository` + SPA 专用 Token Handler；退出登录会使会话失效并重新签发令牌。
- **密码**：BCrypt cost 12。
- **RBAC**：权限编码作为 Spring Security Authority，通过启用状态的角色授予；控制器使用 `@PreAuthorize`。
- **菜单**：仅返回当前用户拥有对应权限编码的菜单。
- **控制台数据**：全局指标需要 `admin:dashboard:read`；普通用户看到个人入口面板。
- **账号状态**：禁用账号无法登录；禁止禁用/删除最后一个启用管理员。
- **审计**：登录失败也会记录；`@AuditLog` 使用 `REQUIRES_NEW`，业务事务回滚不影响审计记录。
- **会话时效**：角色/权限变更对已登录会话在下次登录时生效。小型单体有意采用该取舍；如需即时撤销，可增加权限缓存与版本号。

---

## 数据库与迁移

- Flyway 管理 Schema，Hibernate 使用 `ddl-auto=validate`。
- 迁移文件：`backend/src/main/resources/db/migration/V1__init.sql`、`V2__seed.sql`。
- 表：`sys_user`、`sys_role`、`sys_permission`、`sys_menu`、`sys_user_role`、`sys_role_permission`、`sys_login_log`、`sys_operation_log`、`sys_config`。
- 种子数据包含 `ADMIN`/`USER` 角色、29 个权限、9 个菜单、4 个配置项与 `admin` 账号。

重置本地数据库：

```sql
DROP DATABASE mxxssm_admin;
```

重启应用后 Flyway 会自动重建。

---

## 常用命令

| 命令 | 说明 |
| --- | --- |
| `pnpm dev` | 同时启动后端与 Vite |
| `pnpm dev:api` | 仅启动 Spring Boot |
| `pnpm dev:web` | 仅启动 Vite |
| `pnpm build` | 构建前端与单 Jar |
| `pnpm test` | 后端 + 前端测试 |
| `pnpm test:api` | 后端测试（H2 + MockMvc + ArchUnit） |
| `pnpm test:web` | Vitest 单元测试 |
| `pnpm lint` | ESLint |
| `pnpm format` | Prettier |

---

## 测试

- **后端**：14 个测试，覆盖登录、CSRF、RBAC、用户 CRUD 全链路（H2 + Flyway），以及 ArchUnit 架构规则。
- **前端**：9 个 Vitest 单测，覆盖权限判断、菜单分组与格式化工具。

```bash
pnpm test
```

后端测试 Profile（`application-test.yml`）使用 MySQL 模式的内存 H2，无需 Docker。

---

## 与旧版 SSM/JSP 的对应关系

| 旧实现 | 新实现 |
| --- | --- |
| Spring MVC XML + `web.xml` | Spring Boot 自动配置 |
| WAR + Tomcat 8.5/9 | 可执行 Jar + 内嵌 Tomcat 10.1 |
| JSP + JSTL + jQuery | Vue 3 SPA + Element Plus |
| MyBatis XML Mapper | Spring Data JPA + Specification |
| 手写 Token Cookie + 拦截器 | Spring Security Session + CSRF + 方法级权限 |
| `applicationContext.xml` 数据源 | `application.yml` + HikariCP |
| 手写 SQL 建表 | Flyway 版本化迁移 |
| 自定义 JSON 工具 | Jackson + 统一响应结构 |
| 手写分页/表格 JS | 类型安全 API 模块 + Element Plus |

旧代码保留在 Git 历史中。

---

## 常见问题

| 现象 | 处理方式 |
| --- | --- |
| `Port 8081 already in use` | 修改 `server.port` 或停止占用进程 |
| MySQL `Access denied` | 设置 `DB_USERNAME`/`DB_PASSWORD`，并确认用户有建库权限 |
| 登录提示 CSRF 失效 | 统一通过 Vite 代理或 Jar 访问，不要混用端口 |
| Flyway 校验失败 | 不要修改已执行的迁移文件，新增 `V3__...sql` |
| 生产环境没有 Swagger | 设置 `SPRINGDOC_ENABLED=true`（开发环境默认启用） |
| H2 测试失败 | 执行 `pnpm test:api`，测试使用独立内存数据库 |

---

## 目录结构

```text
MXXSSM/
├── backend/                  Spring Boot 应用、Flyway、测试
│   └── src/main/java/com/mxxssm/
│       ├── common/           响应结构、异常、安全、审计、Web Filter
│       ├── config/           OpenAPI、MVC、配置属性
│       └── modules/          auth/user/role/permission/menu/config/log/dashboard
├── frontend/                 Vue 3 SPA、Element Plus、国际化、单元测试
├── docs/                     架构与 API 文档
├── .github/workflows/        CI
├── Dockerfile                多阶段构建
├── docker-compose.yml        MySQL 8.4 + 应用
├── pom.xml                   Maven 父工程
└── package.json              根目录开发脚本
```

更多设计说明见 [docs/architecture.md](docs/architecture.md)。

---

## 许可证

[MIT](LICENSE)
