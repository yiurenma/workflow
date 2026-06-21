# Workflow Studio 使用指南（用户向）

> **这是给"使用者"看的文档**，区别于 `../`（内部团队的 PM/Arch/Test 过程文档）。
> 一句话：**不写后端代码，把"消息丰富化 + 条件判断 + 多通道下发"编排成一个可调用的 API。**

## 从这里开始

| 你是… | 先读 |
|---|---|
| 第一次听说，想知道这是什么 | [产品说明书](01-product-manual.md) |
| 想立刻动手做出第一个 API | [入门指南（5 分钟）](02-getting-started.md) |
| 想搞清楚核心概念/词汇 | [核心概念](03-concepts.md) |
| 要认真搭东西、查语法 | [语言参考手册](#语言参考手册) |
| 我是调用方，要调别人做的 API | [调用已发布 API](reference/api-call.md) |

## 语言参考手册

- [节点类型参考](reference/nodes.md) — 6 种节点的字段与用法
- [规则语言（JSONPath）](reference/rules-jsonpath.md) — 条件怎么写
- [工作流 JSON Schema](reference/workflow-json.md) — 导入/导出的"语法"
- [调用已发布 API](reference/api-call.md) — 路径 / 头 / 参数 / 响应
- [错误码表](reference/error-codes.md) — WF-* 与 M00xx

## 示例

- [`examples/payment-notify.workflow.json`](examples/payment-notify.workflow.json) — 一个真实、可导入的示例工作流（CONSUMER → IFELSE → MESSAGE）。本指南全程以它为主线。

## 学习路径

1. 读[产品说明书](01-product-manual.md)（5 分钟，建立认知）
2. 跟[入门指南](02-getting-started.md)做出并调用第一个 API（15 分钟）
3. 需要时回[参考手册](#语言参考手册)查具体字段/错误码

---
*推广/对外材料见 [`../promo/`](../promo/)。*
