-- Seed data. The default administrator is:
--   username: admin
--   password: Admin@123
-- Change it immediately after the first login.

INSERT INTO sys_role (id, code, name, description, status) VALUES
    (1, 'ADMIN', 'Administrator', 'Full access to the administration area.', 'ACTIVE'),
    (2, 'USER', 'User', 'Standard user with access to the personal area.', 'ACTIVE');

INSERT INTO sys_permission (id, code, name, description, status) VALUES
    (1,  'admin:user:read',              'Read users',                'List and inspect accounts.', 'ACTIVE'),
    (2,  'admin:user:create',            'Create users',              'Create administrator or user accounts.', 'ACTIVE'),
    (3,  'admin:user:update',            'Update users',              'Update account details, status and roles.', 'ACTIVE'),
    (4,  'admin:user:delete',            'Delete users',              'Delete accounts.', 'ACTIVE'),
    (5,  'admin:role:read',              'Read roles',                'List roles.', 'ACTIVE'),
    (6,  'admin:role:create',            'Create roles',              'Create roles.', 'ACTIVE'),
    (7,  'admin:role:update',            'Update roles',              'Update role metadata and status.', 'ACTIVE'),
    (8,  'admin:role:delete',            'Delete roles',              'Delete custom roles.', 'ACTIVE'),
    (9,  'admin:role:permission:read',   'Read role permissions',     'Inspect permissions assigned to a role.', 'ACTIVE'),
    (10, 'admin:role:permission:update', 'Update role permissions',   'Assign or revoke permissions for a role.', 'ACTIVE'),
    (11, 'admin:menu:read',              'Read menus',                'List navigation menus.', 'ACTIVE'),
    (12, 'admin:menu:create',            'Create menus',              'Create navigation menus.', 'ACTIVE'),
    (13, 'admin:menu:update',            'Update menus',              'Update navigation menus.', 'ACTIVE'),
    (14, 'admin:menu:delete',            'Delete menus',              'Delete navigation menus.', 'ACTIVE'),
    (15, 'admin:permission:read',        'Read permissions',          'List permission definitions.', 'ACTIVE'),
    (16, 'admin:permission:create',      'Create permissions',        'Create permission definitions.', 'ACTIVE'),
    (17, 'admin:permission:update',      'Update permissions',        'Update permission definitions.', 'ACTIVE'),
    (18, 'admin:permission:delete',      'Delete permissions',        'Delete permission definitions.', 'ACTIVE'),
    (19, 'admin:log:login:read',         'Read login logs',           'Inspect login audit records.', 'ACTIVE'),
    (20, 'admin:log:operation:read',     'Read operation logs',       'Inspect operation audit records.', 'ACTIVE'),
    (21, 'admin:config:read',            'Read system config',        'List system configuration entries.', 'ACTIVE'),
    (22, 'admin:config:create',          'Create system config',      'Create system configuration entries.', 'ACTIVE'),
    (23, 'admin:config:update',          'Update system config',      'Update system configuration entries.', 'ACTIVE'),
    (24, 'admin:config:delete',          'Delete system config',      'Delete system configuration entries.', 'ACTIVE'),
    (25, 'user:profile:read',            'Read own profile',          'View the personal profile page.', 'ACTIVE'),
    (26, 'user:profile:update',          'Update own profile',        'Update the personal profile.', 'ACTIVE'),
    (27, 'user:password:update',         'Change own password',       'Change the account password.', 'ACTIVE'),
    (28, 'user:settings:read',           'Read personal settings',    'View personal preferences.', 'ACTIVE');

INSERT INTO sys_user (id, username, password, display_name, email, status) VALUES
    (1, 'admin', '$2a$12$omjuIK0WYvRl2ocK9dIO2e7hD5B29tXl7gDci/S9pLb1XK93D02Se', 'Administrator', 'admin@mxxssm.local', 'ACTIVE');

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission WHERE code IN ('user:profile:read', 'user:profile:update', 'user:password:update', 'user:settings:read');

INSERT INTO sys_menu (id, code, name, path, icon, group_name, permission_code, sort_order, status) VALUES
    (1, 'users',          'Users',          '/system/users',          'User',       'System',  'admin:user:read',          10, 'ACTIVE'),
    (2, 'roles',          'Roles',          '/system/roles',          'UserFilled', 'System',  'admin:role:read',          20, 'ACTIVE'),
    (3, 'menus',          'Menus',          '/system/menus',          'Menu',       'System',  'admin:menu:read',          30, 'ACTIVE'),
    (4, 'permissions',    'Permissions',    '/system/permissions',    'Key',        'System',  'admin:permission:read',    40, 'ACTIVE'),
    (5, 'login-logs',     'Login logs',     '/system/login-logs',     'Document',   'System',  'admin:log:login:read',     50, 'ACTIVE'),
    (6, 'operation-logs', 'Operation logs', '/system/operation-logs', 'Tickets',    'System',  'admin:log:operation:read', 60, 'ACTIVE'),
    (7, 'configs',        'System config',  '/system/configs',        'Setting',    'System',  'admin:config:read',        70, 'ACTIVE'),
    (8, 'profile',        'Profile',        '/profile',               'Avatar',     'Account', 'user:profile:read',        10, 'ACTIVE'),
    (9, 'settings',       'Settings',       '/settings',              'Tools',      'Account', 'user:settings:read',       20, 'ACTIVE');

INSERT INTO sys_config (config_key, config_value, description, status) VALUES
    ('site.name', 'MXXSSM', 'Application display name.', 'ACTIVE'),
    ('security.captcha.enabled', 'true', 'Require a captcha on the login form.', 'ACTIVE'),
    ('security.registration.enabled', 'true', 'Allow public self-registration.', 'ACTIVE'),
    ('ui.default-locale', 'zh-CN', 'Default UI locale (zh-CN or en-US).', 'ACTIVE');
