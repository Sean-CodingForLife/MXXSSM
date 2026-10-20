-- MXXSSM schema, intentionally written in portable SQL so it also runs on H2 (MySQL mode)
-- for integration tests. MySQL 8 is the production target.

CREATE TABLE sys_user (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL,
    password      VARCHAR(100) NOT NULL,
    display_name  VARCHAR(64)  NOT NULL,
    email         VARCHAR(128),
    phone         VARCHAR(32),
    status        VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    last_login_at DATETIME,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_user_username UNIQUE (username)
);

CREATE TABLE sys_role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(64) NOT NULL,
    name        VARCHAR(64) NOT NULL,
    description VARCHAR(255),
    status      VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_role_code UNIQUE (code)
);

CREATE TABLE sys_permission (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL,
    name        VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    status      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_permission_code UNIQUE (code)
);

CREATE TABLE sys_menu (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    code            VARCHAR(64)  NOT NULL,
    name            VARCHAR(128) NOT NULL,
    path            VARCHAR(255) NOT NULL,
    icon            VARCHAR(64),
    group_name      VARCHAR(64)  NOT NULL DEFAULT 'General',
    permission_code VARCHAR(64)  NOT NULL,
    sort_order      INT          NOT NULL DEFAULT 0,
    status          VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_menu_code UNIQUE (code)
);

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE
);

CREATE TABLE sys_role_permission (
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission (id) ON DELETE CASCADE
);

CREATE TABLE sys_login_log (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(64),
    status     VARCHAR(16) NOT NULL,
    ip         VARCHAR(64),
    user_agent VARCHAR(255),
    message    VARCHAR(255),
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_operation_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(64),
    action      VARCHAR(100) NOT NULL,
    method      VARCHAR(16)  NOT NULL,
    path        VARCHAR(255) NOT NULL,
    status      VARCHAR(16)  NOT NULL,
    message     VARCHAR(255),
    duration_ms BIGINT       NOT NULL DEFAULT 0,
    ip          VARCHAR(64),
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_config (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key   VARCHAR(128) NOT NULL,
    config_value VARCHAR(500) NOT NULL,
    description  VARCHAR(255),
    status       VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sys_config_key UNIQUE (config_key)
);

CREATE INDEX idx_sys_user_status ON sys_user (status);
CREATE INDEX idx_sys_user_role_role_id ON sys_user_role (role_id);
CREATE INDEX idx_sys_role_permission_permission_id ON sys_role_permission (permission_id);
CREATE INDEX idx_sys_login_log_created_at ON sys_login_log (created_at);
CREATE INDEX idx_sys_operation_log_created_at ON sys_operation_log (created_at);
CREATE INDEX idx_sys_menu_sort_order ON sys_menu (sort_order);
