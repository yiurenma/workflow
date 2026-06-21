# Architecture Document v42.0 (Revised)

**Document version:** 42.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Label:** `TODO-deploy-step1-cors-proxy-for-target-base-url`

## Problem

浏览器 CORS 策略阻止 Deploy 请求到用户填写的目标 URL。

## Solution

前端在 `fetch` 中添加 `mode: 'cors'`,文档说明目标服务器需配置 CORS 头。

## Frontend Changes

**File:** `workflow-ui/src/components/DeployModal.tsx`

所有 `fetch` 调用添加:
```javascript
{
  method: "POST", // or PATCH
  headers,
  body,
  mode: 'cors',  // 新增
  credentials: 'omit'  // 新增
}
```

## Documentation

在 Deploy 模态框添加说明: "目标服务器需配置 CORS 头允许来自 workflow-ui-gamma.vercel.app 的请求"
