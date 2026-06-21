# Product Requirements Document v42.0

**Document version:** 42.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Label:** `TODO-deploy-step1-cors-proxy-for-target-base-url`

## Problem

浏览器直接调用用户填写的 Deploy URL 触发 CORS 错误,Deploy 失败。

## Solution

在 operation-api 添加代理端点 `/workflow/deploy/proxy`,前端通过同源后端转发请求。

## User Story

**As a** 用户  
**I want** Deploy 功能正常工作  
**So that** 我能将工作流部署到远程环境

**AC:**
- AC-42.1: 前端调用 operation-api 代理端点而非直接跨域
- AC-42.2: 代理端点转发 POST/PATCH 请求到目标 URL
- AC-42.3: 代理端点转发 Basic Auth 头
- AC-42.4: Deploy 三步流程成功完成
