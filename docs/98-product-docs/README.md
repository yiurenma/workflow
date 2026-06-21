# 产品文档索引

## 📖 这是什么？
面向**外部用户**的产品使用文档。让用户了解产品、快速上手、查阅参考。

## 📊 文档状态

| 文档 | 状态 | 最后更新 |
|------|------|----------|
| [01-product-manual.md](01-product-manual.md) | ✅ 当前 | - |
| [02-getting-started.md](02-getting-started.md) | ✅ 当前 | - |
| [03-concepts.md](03-concepts.md) | ✅ 当前 | - |
| [CHANGELOG.md](CHANGELOG.md) | ✅ v43.0 | 2026-04-19 |

## 📍 快速导航

| 我要... | 去哪里 |
|---------|--------|
| 📖 了解产品是什么 | [01-product-manual.md](01-product-manual.md) |
| ⚡ 5 分钟上手 | [02-getting-started.md](02-getting-started.md) |
| 🧠 理解核心概念 | [03-concepts.md](03-concepts.md) |
| 📝 查看更新日志 | [CHANGELOG.md](CHANGELOG.md) |
| 📚 查阅 API 参考 | [reference/api-call.md](reference/api-call.md) |
| 🔧 查看节点类型 | [reference/nodes.md](reference/nodes.md) |
| ⚠️ 查看错误码 | [reference/error-codes.md](reference/error-codes.md) |

## 📂 文档结构

```
01-product-manual.md      产品说明书（功能总览）
02-getting-started.md     快速入门（5 分钟教程）
03-concepts.md            核心概念（应用、工作流、节点、规则）
CHANGELOG.md              版本更新日志
index.md                文档导航（如果有）
reference/                参考文档
  ├── api-call.md           → API 调用接口
  ├── error-codes.md        → 错误码表
  ├── nodes.md              → 节点类型参考
  ├── rules-jsonpath.md     → 规则语法（JSONPath）
  └── workflow-json.md      → 工作流 JSON 格式
```

## 📝 文档维护规则

**CHANGELOG 是关键** - 每个用户可见的 TODO 完成后，**必须**在 CHANGELOG.md 添加一行

**保持真实** - 文档描述当前已实现的功能，未上线的功能标注"路线图"

**与内部文档对齐** - reference/ 内容应与 Architect 文档的接口定义一致

## 🔗 相关内部文档

- 产品需求 → [../02-PM/baseline.md](../02-PM/baseline.md)
- 架构设计 → [../03-Architect/README.md](../03-Architect/README.md)
