# Product Requirements Document — v38.0

**Document Version:** 38.0  
**Date:** 2026-04-19  
**Status:** Draft  

**Scope:** Two features for Workflow Platform:
1. Hub — ApplicationName Deploy action
2. Canvas — Import Workflow from JSON

---

## Feature 1: Hub — ApplicationName Deploy

### APP-US-52 — Deploy application to remote environment

> **As** an integration engineer, **I want** to deploy an application name and its workflow to a remote environment (e.g., UAT operation-api) from the Hub, **so that** I can push my local configuration to a target deployment without manual API calls.

**Desktop:** APP-AC-52-D1 Deploy button in ApplicationName action area · APP-AC-52-D2 Modal shows feature explanation before form · APP-AC-52-D3 Form collects: Deploy URL (base only, e.g., UAT operation-api), application name, service account username, service account password, environment (5 fields total) · APP-AC-52-D4 System auto-appends three paths to base URL and calls in sequence: `CreateApplicationName` → `UpdateApplicationName` → `SaveWorkflow` · APP-AC-52-D5 UI shows three-step progress indicator · APP-AC-52-D6 Success state: all three steps succeed, green success indicator displayed · APP-AC-52-D7 Failure state: clear error message indicating which step failed

**Mobile:** APP-AC-52-M1 Deploy action available in overflow menu · APP-AC-52-M2 Form fields and flow identical to desktop · APP-AC-52-M3 Progress and success/failure indicators adapted for mobile viewport

**Common:** APP-AC-52-G1 Deploy operates on currently loaded application context · APP-AC-52-G2 Application name field in form can differ from currently loaded application (enables deploying to different name) · APP-AC-52-G3 Three API calls execute sequentially: create application name, update application data (aligns with Hub Save semantics on application side), save workflow · APP-AC-52-G4 All three steps must succeed for overall success · APP-AC-52-G5 Service account credentials used for authentication to target environment

**UAT Key Test Case:** In context of an already-loaded existing application, fill form with a different application name, use UAT URL, verify end-to-end success (all three API calls succeed, application deployed to target environment with new name).

---

## Feature 2: Canvas — Import Workflow from JSON

### CV-US-53 — Import workflow from JSON with validation

> **As** a workflow author, **I want** to paste or upload a JSON string representing a complete workflow and have the system validate it before applying to canvas, **so that** I can import externally produced or hand-edited workflows with confidence that syntax errors will be caught before replacing my current work.

**Desktop:** CV-AC-53-D1 Import action in canvas header toolbar · CV-AC-53-D2 Modal allows paste or file upload of JSON string · CV-AC-53-D3 System strips markdown code fences if present (handles users pasting fenced blocks) · CV-AC-53-D4 JSON parsed and validated against WorkFlow schema (required fields, plugin types, unique IDs, edge endpoints exist) · CV-AC-53-D5 Validation errors shown with human-readable messages and line/path hints where possible · CV-AC-53-D6 Preview summary displayed (node count, warnings) · CV-AC-53-D7 Apply button replaces current canvas workflow · CV-AC-53-D8 Confirmation prompt if canvas is non-empty (PM decides copy vs destructive default) · CV-AC-53-D9 In-UI instructions explain input must be valid workflow JSON matching app format · CV-AC-53-D10 Expandable "What belongs here?" section with minimal example snippet · CV-AC-53-D11 Instructions clarify this is NOT for plain English (use Generate for that) · CV-AC-53-D12 Note that Save still persists to backend as today

**Mobile:** CV-AC-53-M1 Import action in mobile overflow menu · CV-AC-53-M2 Modal and validation flow identical to desktop · CV-AC-53-M3 File upload adapted for mobile file picker

**Common:** CV-AC-53-G1 Separate from AI Generate (WorkflowGeneratorModal) — this is for structured JSON validation, not natural language · CV-AC-53-G2 Primary use case: validate externally produced or hand-edited JSON before replacing canvas · CV-AC-53-G3 Validation is client-side (workflow-ui only, no new backend) · CV-AC-53-G4 Carbon modal patterns match v28 standards · CV-AC-53-G5 Save button disabled if validation errors present

**Recognition label:** `TODO-canvas-import-workflow-json-validate-apply`

---

## Non-Functional Requirements

- Deploy feature must handle network failures gracefully (timeout, connection error)
- Import validation must complete within 2 seconds for workflows up to 100 nodes
- Both features maintain WCAG 2.1 AA accessibility compliance
- Carbon Design System consistency maintained across all new UI components

---

## Out of Scope

- Deploy to multiple environments simultaneously
- Deploy history or rollback from Deploy modal
- Import from formats other than JSON (YAML, XML)
- Server-side validation for Import (all validation is client-side)
- Automatic schema migration for imported workflows

---

## Acceptance Criteria Summary

**Deploy (APP-US-52):**
- Deploy button visible in ApplicationName action area (desktop + mobile)
- Modal collects 5 required fields
- Three sequential API calls execute with progress indication
- Success requires all three steps to pass
- UAT test: deploy loaded application to different name on UAT environment

**Import (CV-US-53):**
- Import action in canvas toolbar (desktop) and overflow menu (mobile)
- JSON validation catches schema violations before apply
- Human-readable error messages with line/path hints
- Preview summary before apply
- Confirmation if canvas non-empty
- In-UI instructions with example snippet
