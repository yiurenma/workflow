#!/usr/bin/env bash
# One-shot: capture fresh media from UAT (gamma) + build artifacts/workflow-studio-product-demo.mp4
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
"${ROOT}/scripts/capture-uat-demo-media.sh"
"${ROOT}/scripts/generate-product-demo-video.sh"
