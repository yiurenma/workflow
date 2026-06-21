# Test Doc v6.0 — Explain (GitHub Copilot Path): Richer Prompt + Markdown Response UI

**Feature:** `TODO-explain-github-copilot-prompt-markdown-ui`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Test Cases — Source / Static

| TC-ID | Description | Expected |
|-------|-------------|----------|
| TC-34-L01 | `buildExplainPrompt` includes full `ruleList` (key + remark) per node | Source contains `plugin.ruleList?.map(...)` including `.key` and `.remark` |
| TC-34-L02 | Prompt includes `action.provider` | Source references `plugin.action?.provider` |
| TC-34-L03 | Prompt includes `action.httpRequestMethod` | Source references `plugin.action?.httpRequestMethod` |
| TC-34-L04 | Prompt includes `action.httpRequestBody` (truncated) | Source references `httpRequestBody` with truncation logic |
| TC-34-L05 | Prompt includes `action.elseLogic` (truncated) | Source references `elseLogic` with truncation logic |
| TC-34-L06 | Prompt instructs model to explain each rule in plain English | Prompt text contains instruction about rule explanation |
| TC-34-L07 | Prompt instructs model to explain each action | Prompt text contains instruction about action explanation |
| TC-34-L08 | Prompt requests Markdown output format | Prompt text specifies `##` heading per step or equivalent Markdown format instruction |
| TC-35-L01 | Explain result modal uses `<ReactMarkdown>` or equivalent, NOT `<pre>` | `<pre>` removed from explainResult render path; `ReactMarkdown` or custom renderer used |
| TC-35-L02 | `react-markdown` in `package.json` dependencies | `package.json` contains `"react-markdown"` |
| TC-35-L03 | No `rehype-raw` plugin used | Source does NOT import or use `rehype-raw` (XSS protection) |
| TC-35-L04 | Run modal, token modal, device flow modal NOT changed | Diff shows no changes to `runOpen`, `tokenPromptOpen`, `deviceFlowOpen` modal JSX |
| TC-35-L05 | `callAI` function unchanged (both Anthropic + GitHub paths work) | `callAI` signature and paths identical to pre-change |

---

## Browser Test Cases (UAT — human executes)

| TC-ID | Step | What to verify |
|-------|------|----------------|
| TC-34-B01 | Open a workflow with at least 2 nodes that have rules configured; click **Explain** | Explanation output contains a section per node; each node section mentions its rules in plain English |
| TC-34-B02 | Open a workflow with an HTTP_CALL node; click **Explain** | Explanation mentions the URL/method of the HTTP call action |
| TC-34-B03 | Open a workflow with an IF_ELSE node with `elseLogic`; click **Explain** | Explanation mentions the logic/payload and what it does |
| TC-35-B01 | Click **Explain** on any workflow | Response renders with headings (larger text, not `##` raw text) and bullet lists (not `* ` or `- ` raw text) |
| TC-35-B02 | AI response contains a code or inline code block | Code block renders with monospace font + background shading, not raw backticks |
| TC-35-B03 | Click **Run** | Run modal opens normally; no regression |
| TC-35-B04 | Clear token, click **Explain** again | Device flow / manual token prompt still works |

---

## Pass Criteria

- All TC-34-L01..L08 PASS (prompt enrichment source checks)
- All TC-35-L01..L05 PASS (Markdown UI source checks)
- All TC-34-B01..B03 PASS (richer prompt browser UAT)
- All TC-35-B01..B04 PASS (Markdown rendering UAT)

---

*Status: Draft*
