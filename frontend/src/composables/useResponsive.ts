import { onMounted, onUnmounted, ref } from 'vue'

export function useResponsive(breakpoint = 900) {
  const isMobile = ref(false)
  let query: MediaQueryList | null = null

  const update = (event: MediaQueryListEvent) => {
    isMobile.value = event.matches
  }

  onMounted(() => {
    query = window.matchMedia(`(max-width: ${breakpoint}px)`)
    isMobile.value = query.matches
    query.addEventListener('change', update)
  })

  onUnmounted(() => {
    query?.removeEventListener('change', update)
  })

  return { isMobile }
}
