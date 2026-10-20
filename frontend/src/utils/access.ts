export function canAccess(permissions: Iterable<string>, code?: string | null): boolean {
  if (!code) {
    return true
  }
  for (const permission of permissions) {
    if (permission === code) {
      return true
    }
  }
  return false
}
