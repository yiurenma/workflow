# Workflow Studio — 产品演示视频（仓库内一键生成）

本仓库在 `workflow-ui/e2e/uat-v27-screenshots/` 中保留了 **Playwright UAT 的真实界面截图**（1280×720），以及 `workflow-ui/src/assets/logo.png`。  
脚本 `scripts/generate-product-demo-video.sh` 使用 **FFmpeg** 将这些素材拼接为 **1920×1080 / 30fps** 的 MP4，并叠加 **Carbon 风格深蓝底 + 标题条 + 暗角**，突出：

- 可视化画布（React Flow）与最终流水线
- 应用列表、表格、设置/规则面板
- 状态标签与导航
- 产品 Logo 开场与收尾 CTA

## 生成视频

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

## 用最新界面替换素材

1. 在 `workflow-ui` 中运行 Playwright / UAT，将新截图保存到 `e2e/uat-v27-screenshots/`（或修改脚本中的路径列表）。
2. 重新执行生成脚本。

## 旁白与配乐

当前脚本 **仅生成画面 + 静音 AAC 轨**（兼容播放器）。若需要旁白或 BGM：

- 在脚本各 `ffmpeg` 段将 `anullsrc` 换为 `-i voiceover.wav` / `-i music.mp3`，并用 `amix` 或 `-map` 选择音轨；或
- 在 DaVinci / Premiere / CapCut 中导入生成的 MP4 再精剪。

## 分镜顺序（约 35s 快速版）

| 顺序 | 素材 | 文案要点 |
|------|------|----------|
| 1 | logo | 品牌开场 |
| 2 | step1-header | 导航与企业级体验 |
| 3 | step2-app-list | 多应用治理 |
| 4 | step4-table | 数据面一览 |
| 5 | step5b-canvas | 画布拖拽编排 |
| 6 | step5-final | 全链路可视化 |
| 7 | step5c-settings-modal | 规则与步骤 |
| 8 | step3-status-tags | 发布/草稿状态 |
| 9 | step5-final | CTA 收尾 |

若要 **90s 电影感版本**，复制脚本中的 `segment_image` 调用、拉长 `dur` 秒数，并在外部工具中加入运镜与调色。
