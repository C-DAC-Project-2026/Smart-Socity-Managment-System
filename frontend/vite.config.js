import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // host: true binds the dev server to 0.0.0.0 instead of just localhost,
    // so other devices on your Wi-Fi/LAN can open it via this machine's IP
    // (e.g. http://192.168.1.5:5173). Without this, only this machine can
    // reach it at all.
    host: true,
    port: 5173,
  },
})
