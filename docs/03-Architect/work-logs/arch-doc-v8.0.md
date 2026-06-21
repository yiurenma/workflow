# Arch Doc v8.0 — Hosting: Point APIs to DigitalOcean URLs

**Feature:** `TODO-hosting-digitalocean-urls`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Files to Change

### `workflow-ui/vercel.json`

```json
// Before
"destination": "https://workflow-operation-api.onrender.com/api/$1"
"destination": "https://workflow-online-api.onrender.com/api/$1"

// After
"destination": "https://workflow-operation-api-n9sbp.ondigitalocean.app/api/$1"
"destination": "https://workflow-online-api-nr3e4.ondigitalocean.app/api/$1"
```

The `/api` path suffix and rewrite source patterns (`/api/proxy/operation/(.*)` and `/api/proxy/online/(.*)`) are unchanged. DigitalOcean App Platform serves the same Spring Boot app at the same `/api/*` path convention.

### `workflow-ui/vite.config.ts`

```ts
// Before
const RENDER_OPERATION = 'https://workflow-operation-api.onrender.com'
const RENDER_ONLINE    = 'https://workflow-online-api.onrender.com'

// After
const DO_OPERATION = 'https://workflow-operation-api-n9sbp.ondigitalocean.app'
const DO_ONLINE    = 'https://workflow-online-api-nr3e4.ondigitalocean.app'
```

Rename the constants from `RENDER_*` to `DO_*` so the naming reflects reality. Update their usages in the `server.proxy` config.

### `workflow/CLAUDE.md`

Update UAT environment table:

```markdown
| operation-api | https://workflow-operation-api-n9sbp.ondigitalocean.app |
| online-api    | https://workflow-online-api-nr3e4.ondigitalocean.app    |
```

### `workflow-operation-api/AGENTS.md`

Update the "CI / Render" section heading and body to say DigitalOcean. Remove reference to `docs/deploy-render.md` as the authoritative deploy guide.

---

## Repos & Branches Affected

| Repo | Target branch |
|------|--------------|
| `workflow-ui` | `main` |
| `workflow` | `master` |
| `workflow-operation-api` | `main` |

---

## Risk

- Pure config/doc change; no code logic changes.
- DigitalOcean App Platform must already be running and reachable at the new URLs (per TODO description, it is).
- CORS: the same-origin Vercel proxy pattern is unchanged — no new CORS surface.

---

*Status: Draft*
