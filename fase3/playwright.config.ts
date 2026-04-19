import { defineConfig } from '@playwright/test';

/**
 * FASE 3 — ATDD
 * Configuración de Playwright para pruebas de aceptación sobre la API REST
 * de BookingYa.
 *
 * Las pruebas son 100% API (sin navegador), usando el cliente HTTP nativo
 * de Playwright (APIRequestContext), lo que las hace rápidas y estables.
 */
export default defineConfig({
  testDir: './tests',
  timeout: 30_000,
  retries: 1,

  reporter: [
    ['list'],                          // salida en consola
    ['html', { open: 'never' }],       // reporte HTML en playwright-report/
    ['json', { outputFile: 'test-results/results.json' }]
  ],

  use: {
    // URL base del sistema bajo prueba
    baseURL: 'http://localhost:8080/api',
    extraHTTPHeaders: {
      'Content-Type': 'application/json',
      'Accept':       'application/json',
    },
  },
});
