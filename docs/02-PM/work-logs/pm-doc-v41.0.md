# Product Requirements Document v41.0

**Document version:** 41.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Label:** `TODO-ui-form-control-heights-and-button-label-contrast`

## Overview

全应用表单控件高度不一致,视觉杂乱;危险按钮(红色删除等)和主按钮文字对比度不足,文案不可见。需要统一表单控件高度并修复按钮文字可见性问题。

## Problem Statement

**当前问题:**
1. 输入框高度 40px,按钮高度 48px,视觉不对齐
2. 红色危险按钮(`.ant-btn-dangerous`)背景 `#da1e28`,但文字颜色未明确设置为白色,导致文案不显示
3. 蓝色主按钮(`.ant-btn-primary`)可能存在类似问题

**影响:**
- 表单视觉杂乱,不符合 IBM Carbon Design System 规范
- 用户无法看到删除等危险操作的按钮文字,可用性问题
- 品牌一致性受损

## User Stories

### US-UI-01: 统一表单控件高度
**As a** 用户  
**I want** 表单中的输入框和按钮高度一致  
**So that** 界面整洁、对齐,符合设计规范

**Acceptance Criteria:**
- AC-UI-01.1: 所有输入框(`.ant-input`, `.ant-input-number`, `.ant-select-selector`)高度统一为 48px
- AC-UI-01.2: 所有按钮(`.ant-btn`)高度保持 48px
- AC-UI-01.3: 表单中输入框与按钮视觉对齐,无高度差异
- AC-UI-01.4: 移动端和桌面端均保持一致

### US-UI-02: 危险按钮文字可见
**As a** 用户  
**I want** 看到删除等危险按钮的文字  
**So that** 我能确认操作内容,避免误操作

**Acceptance Criteria:**
- AC-UI-02.1: 危险按钮(`.ant-btn-dangerous`)文字颜色明确设置为白色 `#ffffff`
- AC-UI-02.2: 悬停状态文字颜色保持白色
- AC-UI-02.3: 禁用状态文字颜色适当调整但仍可见
- AC-UI-02.4: 对比度符合 WCAG 2.1 AA 标准(至少 4.5:1)

### US-UI-03: 主按钮文字可见
**As a** 用户  
**I want** 看到主按钮(保存、创建等)的文字  
**So that** 我能确认操作内容

**Acceptance Criteria:**
- AC-UI-03.1: 主按钮(`.ant-btn-primary`)文字颜色明确设置为白色 `#ffffff`
- AC-UI-03.2: 悬停状态文字颜色保持白色
- AC-UI-03.3: 禁用状态文字颜色适当调整但仍可见
- AC-UI-03.4: 对比度符合 WCAG 2.1 AA 标准(至少 4.5:1)

## Scope

**In scope:**
- 更新 `carbon-overrides.css` 中的输入框和按钮样式
- 统一表单控件高度为 48px
- 明确设置按钮文字颜色为白色
- 验证所有表单页面(应用列表、画布、记录、设置等)

**Out of scope:**
- 功能逻辑更改
- 后端 API 更改
- 新增组件或功能

## Success Metrics

- 所有表单控件高度统一为 48px
- 所有按钮文字清晰可见,对比度 ≥ 4.5:1
- 零视觉回归(其他样式不受影响)

## Dependencies

无

## Risks

- **Risk:** 更改全局样式可能影响其他组件
  - **Mitigation:** 仅修改 `.ant-btn` 和 `.ant-input` 相关样式,测试所有表单页面
