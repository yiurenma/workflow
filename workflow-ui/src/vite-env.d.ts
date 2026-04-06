/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_OPERATION_API_BASE: string
  readonly VITE_ONLINE_API_BASE: string
  readonly VITE_USE_MOCK?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
