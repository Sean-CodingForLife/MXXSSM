-- Global dashboard metrics are administration data, not personal data.
-- They are exposed only to holders of this permission (the ADMIN role by default).

INSERT INTO sys_permission (id, code, name, description, status)
SELECT 29, 'admin:dashboard:read', 'Read dashboard metrics',
       'View global user, role, permission and audit metrics.', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE code = 'admin:dashboard:read');

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.code = 'ADMIN'
  AND p.code = 'admin:dashboard:read'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_permission rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
