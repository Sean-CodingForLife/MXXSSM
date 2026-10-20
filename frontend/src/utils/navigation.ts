import type { MenuItem } from '@/types/auth'

export interface MenuGroup {
  key: string
  items: MenuItem[]
}

export function groupMenus(menus: MenuItem[]): MenuGroup[] {
  const groups = new Map<string, MenuGroup>()
  const sorted = [...menus].sort((left, right) => left.sortOrder - right.sortOrder)
  for (const menu of sorted) {
    const key = menu.groupName || 'General'
    if (!groups.has(key)) {
      groups.set(key, { key, items: [] })
    }
    groups.get(key)?.items.push(menu)
  }
  return [...groups.values()]
}
