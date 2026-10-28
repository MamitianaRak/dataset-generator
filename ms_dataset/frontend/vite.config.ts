/// <reference types="vitest/config" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  // 5173 is the origin allowed by the CORS configuration of dataset-manager and generator.
  server: { port: 5173, strictPort: true },
  test: { environment: 'node' },
});
