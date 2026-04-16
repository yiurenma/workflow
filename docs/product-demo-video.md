# Workflow Studio — 产品演示视频（仓库内一键生成）

## 推荐：从线上 UAT（gamma）现抓素材

脚本会 **优先** 使用 `workflow-ui/e2e/uat-demo-media/` 下的画面（由 Playwright 打开  
`https://workflow-ui-gamma.vercel.app/workflows` 现场截图 + **1920×1080 录屏**）。

```bash
git submodule update --init --recursive
./scripts/capture-uat-demo-media.sh   # 截图 + session-uat-gamma.webm
./scripts/generate-product-demo-video.sh
# 或一条命令：
./scripts/capture-and-build-demo.sh
```

录屏会额外复制为：`workflow-ui/e2e/uat-demo-media/session-uat-gamma.webm`（便于分享 / 转码）。

---

## 兼容：仓库内旧 UAT 截图包

若尚未运行采集脚本，则回退使用 `workflow-ui/e2e/uat-v27-screenshots/` 中打包的截图（可能较旧），以及 `workflow-ui/src/assets/logo.png`。

脚本 `scripts/generate-product-demo-video.sh` 使用 **FFmpeg** 将素材拼接为 **1920×1080 / 30fps** 的 MP4，并叠加 **Carbon 风格深蓝底 + 标题条 + 暗角**，突出：

- 可视化画布（React Flow）与最终流水线
- 应用列表、表格、设置/规则面板
- 状态标签与导航
- 产品 Logo 开场与收尾 CTA

## 仅生成视频（不重新抓 UAT）

```bash
git submodule update --init --recursive   # 若尚未拉取 workflow-ui
./scripts/generate-product-demo-video.sh
```

默认输出：`artifacts/workflow-studio-product-demo.mp4`（目录已加入 `.gitignore`，不会提交进 Git）。

自定义输出路径：

```bash
./scripts/generate-product-demo-video.sh --out ./my-demo.mp4
```

## 画质与编码速度

通过环境变量调整 x264（默认偏快、体积适中）：

```bash
DEMO_VIDEO_PRESET=veryfast DEMO_VIDEO_CRF=23 ./scripts/generate-product-demo-video.sh
```

- `DEMO_VIDEO_PRESET`：传给 `ffmpeg -preset`（如 `ultrafast`、`veryfast`、`fast`）
- `DEMO_VIDEO_CRF`：越小画质越好、文件越大（建议约 20–28）

## 录屏转 MP4（可选）

```bash
ffmpeg -y -i workflow-ui/e2e/uat-demo-media/session-uat-gamma.webm \
  -c:v libx264 -preset veryfast -crf 22 -c:a aac -b:a 160k \
  artifacts/workflow-studio-uat-session-gamma.mp4
```

## 旁白与配乐

当前脚本 **仅生成画面 + 静音 AAC 轨**（兼容播放器）。若需要旁白或 BGM：

- 在脚本各 `ffmpeg` 段将 `anullsrc` 换为 `-i voiceover.wav` / `-i music.mp3`，并用 `amix` 或 `-map` 选择音轨；或
- 在 DaVinci / Premiere / CapCut 中导入生成的 MP4 再精剪。

## 分镜顺序（新鲜 UAT 素材）

| 顺序 | 素材 | 文案要点 |
|------|------|----------|
| 1 | logo | 品牌开场 |
| 2 | 01b-workflows-viewport | 实时 gamma 视窗 |
| 3 | 01-workflows | 应用与工作流列表 |
| 4 | 02-new-application-dialog（若有） | 新建应用 |
| 5 | 03-canvas / 03b-canvas-full（若有） | 画布与全页 |
| 6 | 04-workflows-return | 返回列表 / 治理 |
| 7 | 05-settings-modal（若有） | 设置与规则 |
| 8 | 01-workflows | CTA 收尾 |

旧截图包仍使用 `step1-header` … `step5-final` 顺序（见脚本内 `else` 分支）。

若要 **90s 电影感版本**，复制脚本中的 `segment_image` 调用、拉长 `dur` 秒数，并在外部工具中加入运镜与调色。
