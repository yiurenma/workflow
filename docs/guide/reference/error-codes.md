# 错误码表

两套服务两套码：**控制面（operation-api）= `WF-*`**；**执行面（online-api）= `M00xx` / `4xxxxx`**。

## 控制面 operation-api（`WF-*`）
管理类操作（CRUD、entity-setting、autoCopy、history、records）。

| 码 | 含义 |
|---|---|
| `WF-400-000` | 请求负载或参数有误 |
| `WF-400-001` | 校验失败 |
| `WF-404-000` | 资源未找到 |
| `WF-409-000` | 业务冲突 |
| `WF-500-000` | 内部服务器错误 |
| `WF-400-101` | 应用名必须恰好存在一个（查询/更新/删除） |
| `WF-400-102` | 更新操作必须带工作流 body |
| `WF-409-101` | 重复键重试后工作流更新仍失败 |
| `WF-409-201` | 存在报表时不能删除工作流（注：当前代码已放开删除、保留记录） |
| `WF-400-202` | 工作流映射的 linkingId 为空 |
| `WF-400-301` | autoCopy 源与目标应用名必须不同 |
| `WF-400-302` | autoCopy 源应用名必须恰好存在一个 |
| `WF-400-303` | autoCopy 目标应用名最多存在一个 |
| `WF-400-401` | 历史查询要求应用名恰好存在一个 |
| `WF-404-101` | 未找到该应用的 entity setting |
| `WF-400-402` | entity setting 的应用名必须恰好存在一个 |

## 执行面 online-api（`M00xx` / `4xxxxx`）
在线调用 `POST /api/workflow`。

| 码 | 含义 |
|---|---|
| `440000` | 请求参数/头无效（如缺 `X-Request-Correlation-Id`） |
| `M0001` | 未找到或找到多个该应用的 entity setting（应用名不唯一/不存在） |
| `M0002` | 按请求关联 ID 检测到重复记录（幂等拒绝） |
| `M0003` | 入站数据格式异常 |
| `M0004` | 按 isSelfRequest + originWorkflowRecordId 检测到重复重试 |
| `500` | 外部/内部服务器错误 |

## 错误响应形态
执行面：
```json
{ "errorInfo": [ { "code": "M0001", "detail": { "cause": "..." } } ],
  "requestCorrelation": "...", "sessionCorrelation": null }
```
控制面：含 `code` / `description` / `requestCorrelation` / `sessionCorrelation`。

## 排错速查
| 现象 | 多半是 |
|---|---|
| 调用直接 400 且 `440000` | 少了 `X-Request-Correlation-Id` 头 |
| 400 + `M0001` | `applicationName` 写错或应用未发布 |
| 400 + `M0002` | 同一关联 ID 重复提交（幂等生效） |
| 删除应用报 `WF-400-101` | 应用名不唯一或不存在 |
