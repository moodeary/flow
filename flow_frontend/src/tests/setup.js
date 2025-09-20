// Test setup file for Vitest
import { beforeEach, afterEach, vi } from 'vitest'

// Mock console warnings/errors during tests unless explicitly testing them
const originalConsole = { ...console }

beforeEach(() => {
  vi.clearAllMocks()
  // Reset global mocks before each test
  global.confirm = vi.fn()
  global.alert = vi.fn()
})

afterEach(() => {
  // Restore console after each test
  Object.assign(console, originalConsole)
})

// Global test utilities
global.mockConsole = () => {
  console.warn = vi.fn()
  console.error = vi.fn()
  console.log = vi.fn()
}

// Mock fetch for API tests
global.fetch = vi.fn()

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
}
global.localStorage = localStorageMock

// Mock window.confirm and window.alert globally
global.confirm = vi.fn()
global.alert = vi.fn()
