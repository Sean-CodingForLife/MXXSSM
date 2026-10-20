import { describe, expect, it } from 'vitest'
import { canAccess } from '@/utils/access'

describe('canAccess', () => {
  const permissions = ['admin:user:read', 'admin:role:read']

  it('allows routes without a permission requirement', () => {
    expect(canAccess(permissions, undefined)).toBe(true)
    expect(canAccess([], null)).toBe(true)
  })

  it('allows an exact permission match', () => {
    expect(canAccess(permissions, 'admin:user:read')).toBe(true)
  })

  it('rejects missing permissions', () => {
    expect(canAccess(permissions, 'admin:user:delete')).toBe(false)
    expect(canAccess([], 'admin:user:read')).toBe(false)
  })
})
