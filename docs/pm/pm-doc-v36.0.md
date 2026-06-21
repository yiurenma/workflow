# Product Requirements Document — v36.0

**Document version:** 36.0  
**Date:** 2026-04-13  
**Status:** Draft  
**TODO item:** `TODO-node-editor-rule-key-json-path-validation`  
**Label:** Node editor — rule key must be one JSONPath

---

## 1. Background

Currently, the node editor's rule key field accepts any text input without validation. Users can accidentally enter invalid JSONPath expressions, comma-separated paths, or free text, leading to runtime errors when the workflow executes.

**Product requirement:** Each rule key field must contain **exactly one valid JSONPath** expression.

---

## 2. Objectives

1. **Validate** rule key input to ensure it's a single valid JSONPath expression
2. **Provide clear feedback** when validation fails, explicitly stating the field accepts only one JSONPath
3. **Block save** when validation errors exist
4. **Improve UX** by validating on blur and before save

---

## 3. User Stories

### US-50 — Rule key validation

> **As a** workflow author configuring node rules,  
> **I want** the system to validate that each rule key is a single valid JSONPath expression,  
> **So that** I get immediate feedback if I enter invalid syntax and avoid runtime errors.

**Acceptance Criteria:**

**AC-50-1:** Rule key field validates on blur (when user leaves the field)

**AC-50-2:** Validation checks:
- Input is not empty
- Input is valid JSONPath syntax (using `jsonpath-plus` library)
- Input does NOT contain comma, semicolon, or multiple paths
- Input does NOT contain free text without JSONPath syntax

**AC-50-3:** Validation error message explicitly states: "Rule key must be a single valid JSONPath expression (e.g., $.data.amount)"

**AC-50-4:** Save button is disabled when any rule key has validation errors

**AC-50-5:** Validation runs before save attempt, showing all errors at once

**AC-50-6:** Valid JSONPath examples shown in placeholder or help text

---

## 4. Scope

**In scope:**
- Client-side validation using `jsonpath-plus` library
- Error message display in node editor form
- Save button disable logic
- Validation on blur and before save

**Out of scope:**
- Server-side validation (can be added later)
- Auto-correction of invalid input
- JSONPath builder UI
- Validation of other fields (description, action)

---

## 5. Success Metrics

- Users cannot save workflows with invalid rule keys
- Clear error messages reduce support tickets about runtime JSONPath errors
- Validation feedback is immediate (on blur)

---

## 6. Dependencies

- `jsonpath-plus` library (already used in JsonPath modal)
- Node editor form state management

---

**End of PM Document v36.0**
