#!/usr/bin/env bash
# Capture fresh screenshots + WebM screen recording from live UAT (gamma) via Playwright.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
UI="${ROOT}/workflow-ui"
cd "$UI"
if [[ ! -d node_modules ]]; then
  echo "==> npm install (workflow-ui)"
  npm install
fi
echo "==> Installing Chromium for Playwright (if needed)"
npx playwright install chromium
echo "==> Running UAT demo capture (gamma)…"
npx playwright test e2e/uat-demo-capture.spec.ts --project="UAT Demo Capture"
OUT_MEDIA="${UI}/e2e/uat-demo-media"
CAP_OUT="${UI}/e2e/uat-demo-capture-output"
# Normalize latest Playwright recording to a stable filename for sharing / ffmpeg
WEBM=$(find "$CAP_OUT" -type f -name '*.webm' 2>/dev/null | sort | tail -1)
if [[ -n "$WEBM" ]]; then
  cp -f "$WEBM" "${OUT_MEDIA}/session-uat-gamma.webm"
  echo "==> Screen recording: ${OUT_MEDIA}/session-uat-gamma.webm"
else
  echo "WARN: No .webm found under ${CAP_OUT}"
fi
echo "==> Screenshots: ${OUT_MEDIA}/"
