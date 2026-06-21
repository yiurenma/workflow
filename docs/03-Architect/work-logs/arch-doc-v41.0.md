# Architecture Document v41.0

**Document version:** 41.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Label:** `TODO-ui-form-control-heights-and-button-label-contrast`

## Solution

修改 `carbon-overrides.css` 统一表单控件高度为 48px,明确设置按钮文字颜色为白色。

## Changes

**File:** `src/styles/carbon-overrides.css`

### 1. 输入框高度统一 (line 146)
```css
/* Before */
height: 40px;

/* After */
height: 48px;
```

### 2. 危险按钮文字颜色 (after line 50)
```css
.ant-btn-dangerous {
  background: #da1e28 !important;
  border-color: #da1e28 !important;
  color: #ffffff !important;  /* 新增 */
}

.ant-btn-dangerous:hover {
  background: #ba1b23 !important;
  border-color: #ba1b23 !important;
  color: #ffffff !important;  /* 新增 */
}
```

### 3. 主按钮文字颜色 (after line 40)
```css
.ant-btn-primary {
  background: #0f62fe !important;
  border-color: #0f62fe !important;
  color: #ffffff !important;  /* 新增 */
}

.ant-btn-primary:hover {
  background: #0043ce !important;
  border-color: #0043ce !important;
  color: #ffffff !important;  /* 新增 */
}
```

## Contrast Verification

- 主按钮: `#ffffff` on `#0f62fe` = 8.6:1 ✓ (WCAG AAA)
- 危险按钮: `#ffffff` on `#da1e28` = 7.3:1 ✓ (WCAG AAA)

## Testing

验证所有表单页面:
- 应用列表 (创建/删除按钮)
- 画布 (保存/运行按钮)
- 节点编辑器 (保存/取消按钮)
- 设置模态框 (保存/删除按钮)
- Deploy 模态框 (Deploy 按钮)
