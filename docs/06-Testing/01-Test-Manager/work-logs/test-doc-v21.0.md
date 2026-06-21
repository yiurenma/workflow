# Test Doc v21.0 — Application Name: Description Field

**Label:** `TODO-application-name-description-db-text-ui-api`
**Date:** 2026-04-08

---

## Test Cases

| TC ID | Layer | Description | Expected |
|-------|-------|-------------|----------|
| TC-DESC-01 | API | `GET /api/workflow/entity-setting` returns `description` field | Response JSON includes `description` (null or string) |
| TC-DESC-02 | API | `PATCH` with `{"description":"My desc"}` persists value | Subsequent GET returns `description: "My desc"` |
| TC-DESC-03 | API | `PATCH` with `{"description":""}` clears value | GET returns `description: ""` or `null` |
| TC-DESC-04 | UI | Description column shows value from API | Applications table "Description" column shows `description` field |
| TC-DESC-05 | UI | Description column shows `—` when null | Row with null description shows em-dash |
| TC-DESC-06 | UI | Settings modal has Description textarea | Textarea pre-filled with current description |
| TC-DESC-07 | UI | Edit description and save persists | After save, column and modal show new value |
| TC-DESC-08 | UI | Description up to 2000 chars accepted | Long text saved successfully |
| TC-DESC-09 | Migration | Existing rows unaffected after migration | All pre-existing apps still load normally |

---

## E2E Automation (Playwright)

```ts
test('TC-DESC-04 description column visible', async ({ page }) => {
  // After mock returns description field
  await expect(page.locator('.ant-table-tbody td').nth(1)).toBeVisible();
});

test('TC-DESC-06 description textarea in Settings modal', async ({ page }) => {
  await page.getByRole('button', { name: /settings/i }).first().click();
  await expect(page.locator('.ant-modal').getByLabel(/description/i)).toBeVisible({ timeout: 5000 });
});
```

(Full implementation after approval.)
