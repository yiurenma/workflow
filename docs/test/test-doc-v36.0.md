# Test Document — v36.0

**Document version:** 36.0  
**Date:** 2026-04-13  
**Status:** Draft  
**TODO item:** `TODO-node-editor-rule-key-json-path-validation`  
**Related docs:** pm-doc-v36.0.md, arch-doc-v36.0.md

---

## 1. Test Cases

### TC-RULE-01: Valid JSONPath accepted
**Steps:**
1. Open node editor
2. Add a rule
3. Enter valid JSONPath: `$.data.amount`
4. Click outside field (blur)

**Expected:** No error, save button enabled

---

### TC-RULE-02: Invalid JSONPath rejected
**Steps:**
1. Open node editor
2. Add a rule
3. Enter invalid text: `not a jsonpath`
4. Click outside field

**Expected:** Error message "Rule key must be a single valid JSONPath expression (e.g., $.data.amount)", save button disabled

---

### TC-RULE-03: Multiple paths rejected
**Steps:**
1. Open node editor
2. Add a rule
3. Enter: `$.data.amount, $.data.currency`
4. Click outside field

**Expected:** Error message about single JSONPath, save button disabled

---

### TC-RULE-04: Empty field rejected
**Steps:**
1. Open node editor
2. Add a rule
3. Leave rule key empty
4. Click outside field

**Expected:** Error message, save button disabled

---

### TC-RULE-05: Save blocked with errors
**Steps:**
1. Open node editor with invalid rule key
2. Try to click Save

**Expected:** Save button disabled, cannot save

---

**End of Test Document v36.0**
