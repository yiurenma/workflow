# Test Doc v20.0 — Applications: Edit Application Name

**Label:** `TODO-application-table-edit-name`
**Date:** 2026-04-08

---

## Test Cases

| TC ID | Description | Steps | Expected |
|-------|-------------|-------|----------|
| TC-RENAME-01 | Rename option accessible from Settings modal | Open Settings modal for an app | "Application Name" input field visible, pre-filled with current name |
| TC-RENAME-02 | Successful rename updates table row | Change name → Save | Table row shows new name; no navigation; success message |
| TC-RENAME-03 | Empty name rejected client-side | Clear name field → Save | Validation error shown; no API call |
| TC-RENAME-04 | Name too long rejected | Enter 256-char name → Save | Validation error shown; no API call |
| TC-RENAME-05 | Duplicate name rejected by API | Enter name of existing app → Save | Error message "name already exists"; row unchanged |
| TC-RENAME-06 | Cancel preserves original name | Open Settings → change name → Cancel | Row unchanged |
| TC-RENAME-07 | Rename works on mobile | Mobile viewport | Settings modal accessible; rename flow completes |

---

## E2E Automation (Playwright)

Add to `applications-desktop.spec.ts`:

```ts
test('TC-RENAME-01 rename input visible in Settings modal', async ({ page }) => {
  await page.getByRole('button', { name: /settings/i }).first().click();
  await expect(page.locator('.ant-modal').getByLabel(/application name/i)).toBeVisible({ timeout: 5000 });
});
```

(Full implementation after approval.)
