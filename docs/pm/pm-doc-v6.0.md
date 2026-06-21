# PM Doc v6.0 — Explain (GitHub Copilot Path): Richer Prompt + Markdown Response UI

**Feature:** Explain — richer prompt hints + Markdown-friendly response UI  
**Label:** `TODO-explain-github-copilot-prompt-markdown-ui`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Problem Statement

When users click **Explain** and the request is sent to **GitHub Models** (gpt-4o-mini via `https://models.inference.ai.azure.com`), two problems reduce the value of the response:

1. **Prompt is too thin**: The current `buildExplainPrompt()` sends step names, types, rule counts, and an HTTP URL only. It does NOT include: what each rule's JSONPath expression means, what each action's payload/logic does, or what the action provider/type combination implies. As a result the AI cannot explain rules or actions meaningfully.

2. **Response is rendered as plain `<pre>` text**: When the model returns Markdown (headings, bullet lists, code blocks), the raw syntax (asterisks, hashes, backticks) appears as literal characters. This makes the output noisy and hard to scan.

---

## User Stories

### US-34 — Richer Prompt for Explain (GitHub Copilot Path)

**As a** developer or business analyst using the Explain feature with a GitHub token,  
**I want** the AI explanation to cover what each rule means (the JSONPath condition, in plain English) and what each action does (the action type, provider, HTTP URL/method, payload or else-logic),  
**So that** I get a genuinely useful per-node breakdown, not just a list of step names.

**Acceptance Criteria:**
- AC-34-1: The prompt includes, for each node: full `ruleList` (key + remark) so the model can explain each rule.
- AC-34-2: The prompt includes, for each node: `action.type`, `action.provider`, `action.httpRequestUrlWithQueryParameter`, `action.httpRequestMethod`, `action.httpRequestBody` (if present), and `action.elseLogic` (if present).
- AC-34-3: The prompt explicitly instructs the model to explain what each rule means in plain English and what each action does.
- AC-34-4: The prompt requests Markdown output with headings and bullet lists.
- AC-34-5: Existing Anthropic path continues to work with the same improved prompt.

### US-35 — Markdown-Friendly Response UI

**As a** developer or business analyst reading the Explain output,  
**I want** the AI's Markdown-formatted response to be rendered with proper headings, lists, and code blocks,  
**So that** the explanation is easy to scan and free of raw Markdown noise.

**Acceptance Criteria:**
- AC-35-1: The Explain result modal renders the response as Markdown (not as a `<pre>` block).
- AC-35-2: Headings (`#`, `##`) are visually larger than body text.
- AC-35-3: Bullet lists (`-`, `*`) are rendered as `<ul>/<li>` elements.
- AC-35-4: Inline code and code blocks are rendered with monospace font and background.
- AC-35-5: No XSS risk from rendering — content from AI response is treated as trusted Markdown-to-HTML conversion, but raw HTML tags in the response must NOT be allowed through.
- AC-35-6: No regression to the token entry flow, device flow, or Run modal.

---

## Out of Scope

- Changing the AI provider or model
- Streaming responses
- Saving/exporting explanations
- Any backend changes

---

*Status: Draft*
