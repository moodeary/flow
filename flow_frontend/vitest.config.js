import { fileURLToPath } from 'node:url'
import { mergeConfig, defineConfig, configDefaults } from 'vitest/config'
import viteConfig from './vite.config'

export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      environment: 'jsdom',
      globals: true,
      setupFiles: ['./src/tests/setup.js'],
      include: [
        'src/tests/**/*.{test,spec}.{js,mjs,cjs,ts,mts,cts,jsx,tsx}'
      ],
      exclude: [
        ...configDefaults.exclude,
        'src/tests/e2e/**/*',
        'e2e/**'
      ],
      root: fileURLToPath(new URL('./', import.meta.url)),
    },
  }),
)
