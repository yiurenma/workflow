# Architecture Document v26.0

**Document version:** 26.0  
**Date:** 2026-04-11  
**Status:** Draft  
**TODO label:** `TODO-vercel-build-missing-dayjs-dependency`

## Overview

Fix critical Vercel build failure caused by missing `dayjs` dependency declaration in `workflow-ui/package.json`.

## Problem

**Error:** "Rollup failed to resolve import 'dayjs' from src/routes/records/index.tsx"

**Root cause:** 
- `dayjs` is imported at `src/routes/records/index.tsx:19`
- Not declared in `package.json` dependencies
- Local dev works (transitive dependency or cache)
- Vercel production build fails (clean environment)

**Impact:** CRITICAL - blocks all UAT deployments

## Solution

Add `dayjs` to dependencies in `workflow-ui/package.json`:

```json
"dependencies": {
  ...
  "dayjs": "^1.11.13",
  ...
}
```

## Technical Details

**File affected:** `workflow-ui/package.json`

**Verification steps:**
1. Add dependency
2. Run `npm install` locally
3. Run `npm run build` - should succeed
4. Commit and push to trigger Vercel rebuild
5. Verify Vercel deployment succeeds

**@xyflow/react verification:** Already present at `^12.4.4` - no action needed

## Rollback Plan

If issues arise, revert the commit:
```bash
git revert HEAD
git push origin main
```

## References

- dayjs documentation: https://day.js.org/
- Usage: `workflow-ui/src/routes/records/index.tsx:19`
