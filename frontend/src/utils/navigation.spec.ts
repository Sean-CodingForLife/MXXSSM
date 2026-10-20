import { describe, expect, it } from 'vitest'
import { groupMenus } from '@/utils/navigation'
import type { MenuItem } from '@/types/auth'

function menu(overrides: Partial<MenuItem>): MenuItem {
  return {
    id: 1,
    code: 'users',
    name: 'Users',
    path: '/system/users',
    icon: 'User',
    groupName: 'System',
    permissionCode: 'admin:user:read',
    sortOrder: 10,
    status: 'ACTIVE',
    ...overrides,
  }
}

describe('groupMenus', () => {
  it('groups menus and sorts them by sort order', () => {
    const groups = groupMenus([
      menu({ id: 2, code: 'roles', path: '/system/roles', sortOrder: 20 }),
      menu({ id: 1, code: 'users', path: '/system/users', sortOrder: 10 }),
      menu({ id: 3, code: 'profile', path: '/profile', groupName: 'Account', sortOrder: 5 }),
    ])

    expect(groups).toHaveLength(2)
    expect(groups[0].key).toBe('Account')
    expect(groups[1].key).toBe('System')
    expect(groups[1].items.map((item) => item.code)).toEqual(['users', 'roles'])
  })

  it('falls back to the General group when no group is set', () => {
    const groups = groupMenus([menu({ groupName: '' })])
    expect(groups[0].key).toBe('General')
  })
})
