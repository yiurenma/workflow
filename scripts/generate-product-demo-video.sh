#!/usr/bin/env bash
# Generate a Workflow Studio product demo MP4 from repo UAT screenshots + logo.
# Fast path: static 1920x1080 framing (scale+crop) + titles + fades — encodes in seconds.
# Requires: ffmpeg, ffprobe
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
UI="${ROOT}/workflow-ui"
SHOTS="${UI}/e2e/uat-v27-screenshots"
LOGO="${UI}/src/assets/logo.png"
OUT_DIR="${ROOT}/artifacts"
OUT="${OUT_DIR}/workflow-studio-product-demo.mp4"
FONT_BOLD="/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
FONT_REG="/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
TMP="${OUT_DIR}/.demo-video-tmp-$$"
FPS=30
WIDTH=1920
HEIGHT=1080
PRESET="${DEMO_VIDEO_PRESET:-ultrafast}"
CRF="${DEMO_VIDEO_CRF:-26}"

usage() {
  echo "Usage: $0 [--out PATH]"
  echo "  Writes a ${WIDTH}x${HEIGHT} @ ${FPS}fps demo under artifacts/."
  echo "  Env: DEMO_VIDEO_PRESET, DEMO_VIDEO_CRF"
}

while [[ "${1:-}" == -* ]]; do
  case "$1" in
    --out) OUT="$2"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1"; usage; exit 1 ;;
  esac
done

command -v ffmpeg >/dev/null || { echo "ffmpeg not found"; exit 1; }
[[ -d "$SHOTS" ]] || { echo "Missing screenshot dir: $SHOTS (run: git submodule update --init)"; exit 1; }
[[ -f "$LOGO" ]] || { echo "Missing logo: $LOGO"; exit 1; }

mkdir -p "$OUT_DIR" "$TMP"
trap 'rm -rf "$TMP"' EXIT

escape_drawtext() {
  printf '%s' "$1" | sed "s/'/'\\\\\\\\''/g; s/:/\\\\:/g; s/%/\\\\%/g"
}

segment_image() {
  local img="$1" dur="$2" title="$3" sub="$4" idx="$5"
  local out="$TMP/part-$(printf '%03d' "$idx").mp4"
  local title_e sub_e
  title_e="$(escape_drawtext "$title")"
  sub_e="$(escape_drawtext "$sub")"

  local fade_in=0.4
  local fade_out=0.5
  local fade_out_st
  fade_out_st="$(awk -v d="$dur" -v fo="$fade_out" 'BEGIN { printf "%.3f", d - fo }')"

  local vf="scale=${WIDTH}:${HEIGHT}:force_original_aspect_ratio=increase:flags=bilinear,\
crop=${WIDTH}:${HEIGHT},\
eq=brightness=0.02:contrast=1.08:saturation=1.06,\
vignette=angle=PI/5:mode=backward,\
drawtext=fontfile=${FONT_BOLD}:text='${title_e}':fontsize=54:fontcolor=white:borderw=3:bordercolor=0x001d6c@0.78:x=72:y=h-172,\
drawtext=fontfile=${FONT_REG}:text='${sub_e}':fontsize=26:fontcolor=0xf4f4f4:borderw=2:bordercolor=black@0.55:x=72:y=h-112,\
fade=t=in:st=0:d=${fade_in},fade=t=out:st=${fade_out_st}:d=${fade_out},format=yuv420p"

  ffmpeg -y -hide_banner -loglevel error \
    -loop 1 -framerate "$FPS" -t "$dur" -i "$img" \
    -f lavfi -i "anullsrc=r=48000:cl=stereo" \
    -vf "$vf" \
    -r "$FPS" -c:v libx264 -preset "$PRESET" -crf "$CRF" -c:a aac -b:a 96k -shortest \
    "$out"
  echo "$out"
}

segment_logo() {
  local dur="$1" title="$2" sub="$3" idx="$4"
  local out="$TMP/part-$(printf '%03d' "$idx").mp4"
  local title_e sub_e
  title_e="$(escape_drawtext "$title")"
  sub_e="$(escape_drawtext "$sub")"

  local fade_in=0.45
  local fade_out=0.55
  local fade_out_st
  fade_out_st="$(awk -v d="$dur" -v fo="$fade_out" 'BEGIN { printf "%.3f", d - fo }')"

  local vf="color=c=0x030910:s=${WIDTH}x${HEIGHT}:d=${dur}:r=${FPS},format=rgb24[bg];\
movie=filename='${LOGO}',scale=-1:320,format=rgba[lg];\
[bg][lg]overlay=(W-w)/2:(H-h)/2-40:format=auto,\
drawtext=fontfile=${FONT_BOLD}:text='${title_e}':fontsize=72:fontcolor=white:borderw=3:bordercolor=0x0f62fe@0.95:x=(w-text_w)/2:y=h-220,\
drawtext=fontfile=${FONT_REG}:text='${sub_e}':fontsize=30:fontcolor=0xa6c8ff:x=(w-text_w)/2:y=h-145,\
vignette=angle=PI/4:mode=backward,\
fade=t=in:st=0:d=${fade_in},fade=t=out:st=${fade_out_st}:d=${fade_out},format=yuv420p"

  ffmpeg -y -hide_banner -loglevel error \
    -f lavfi -i "anullsrc=r=48000:cl=stereo" \
    -filter_complex "$vf" \
    -t "$dur" -r "$FPS" -c:v libx264 -preset "$PRESET" -crf "$CRF" -c:a aac -b:a 96k -shortest \
    "$out"
  echo "$out"
}

echo "==> Building segments (preset=$PRESET crf=$CRF) -> $TMP"

declare -a PARTS=()
i=0

PARTS+=("$(segment_logo 3.2 "Workflow Studio" "低代码 API 编排 · 可视化流水线" "$i")"); i=$((i+1))
PARTS+=("$(segment_image "$SHOTS/step1-header.png" 3.5 "从想法到流水线" "企业级导航与一致体验" "$i")"); i=$((i+1))
PARTS+=("$(segment_image "$SHOTS/step2-app-list.png" 4 "多应用，一套编排" "应用列表 · 快速定位与治理" "$i")"); i=$((i+1))
PARTS+=("$(segment_image "$SHOTS/step4-table.png" 3.5 "数据面一览" "表格视图 · 请求与上下文" "$i")"); i=$((i+1))
PARTS+=("$(segment_image "$SHOTS/step5b-canvas.png" 5 "画布即真理" "React Flow · 拖拽节点与连线" "$i")"); i=$((i+1))
PARTS+=("$(segment_image "$SHOTS/step5-final.png" 5 "复杂流程，一眼可控" "Enrichment → Dispatch 全链路" "$i")"); i=$((i+1))
PARTS+=("$(segment_image "$SHOTS/step5c-settings-modal.png" 3.5 "规则与步骤" "JsonPath 规则 · 步骤配置" "$i")"); i=$((i+1))
PARTS+=("$(segment_image "$SHOTS/step3-status-tags.png" 3.5 "状态清晰可见" "发布 / 草稿 · 运维友好" "$i")"); i=$((i+1))
PARTS+=("$(segment_image "$SHOTS/step5-final.png" 4 "Workflow Studio" "可视化编排 · 安全执行 · 可观测" "$i")"); i=$((i+1))

CONCAT="$TMP/list.txt"
: >"$CONCAT"
for p in "${PARTS[@]}"; do
  printf "file '%s'\n" "$p" >>"$CONCAT"
done

echo "==> Concatenating -> $OUT"
ffmpeg -y -hide_banner -loglevel error -f concat -safe 0 -i "$CONCAT" -c copy "$OUT"

dur="$(ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "$OUT")"
echo "==> Done: $OUT (${dur}s, ${WIDTH}x${HEIGHT} @ ${FPS}fps)"
