# Test Document v41.0

**Document version:** 41.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Label:** `TODO-ui-form-control-heights-and-button-label-contrast`

## Test Cases

### TC-UI-HEIGHT-01: 输入框与按钮高度一致
**验证:** 所有表单页面输入框和按钮高度均为 48px
**页面:** 应用列表、画布、节点编辑器、设置、Deploy

### TC-UI-CONTRAST-01: 危险按钮文字可见
**验证:** 删除等红色按钮文字为白色,对比度 ≥ 4.5:1
**预期:** `#ffffff` on `#da1e28` = 7.3:1 ✓

### TC-UI-CONTRAST-02: 主按钮文字可见
**验证:** 保存/创建等蓝色按钮文字为白色,对比度 ≥ 4.5:1
**预期:** `#ffffff` on `#0f62fe` = 8.6:1 ✓
