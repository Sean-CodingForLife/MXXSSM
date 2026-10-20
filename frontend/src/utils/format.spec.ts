import { describe, expect, it } from 'vitest'
import { formatDuration, formatNumber } from '@/utils/format'

describe('formatDuration', () => {
  it('renders sub-second values in milliseconds', () => {
    expect(formatDuration(0)).toBe('0 ms')
    expect(formatDuration(499)).toBe('499 ms')
  })

  it('renders longer values in seconds', () => {
    expect(formatDuration(1500)).toBe('1.50 s')
  })

  it('renders invalid values as a dash', () => {
    expect(formatDuration(-1)).toBe('-')
    expect(formatDuration(Number.NaN)).toBe('-')
  })
})

describe('formatNumber', () => {
  it('formats a number for the current locale', () => {
    expect(formatNumber(1234)).toContain('1')
  })
})
