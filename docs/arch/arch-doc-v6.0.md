# Arch Doc v6.0 — Explain (GitHub Copilot Path): Richer Prompt + Markdown Response UI

**Feature:** `TODO-explain-github-copilot-prompt-markdown-ui`  
**Date:** 2026-04-07  
**Status:** Draft — awaiting human approval

---

## Current State

### `buildExplainPrompt()` — what it sends per node

```ts
const steps = (workFlow.pluginList ?? []).map((plugin, i) => {
  const type = plugin.action?.type ?? "UNKNOWN";
  const desc = plugin.description ?? `Step ${i + 1}`;
  const rulesCount = plugin.ruleList?.length ?? 0;
  const linkingId = plugin.linkingIdOfRuleListAndAction ?? "";
  const url = plugin.action?.httpRequestUrlWithQueryParameter ? JSON.stringify(...) : null;
  return `  ${i + 1}. [${type}] ${desc} (linkingId: ${linkingId}, rules: ${rulesCount})${url ? ` → ${url}` : ""}`;
});
```

**Missing per node**: the actual rule expressions and remarks, the action provider, HTTP method, request body, and elseLogic payload.

### Explain result modal — current rendering

```tsx
<pre className="whitespace-pre-wrap text-sm text-zinc-700 leading-relaxed font-sans">
  {explainResult}
</pre>
```

Raw Markdown text rendered verbatim — headings and lists appear as literal `#` and `*`.

---

## Proposed Changes

### 1. Enrich `buildExplainPrompt()`

Replace the single-line step summary with a structured per-node block that includes:

```
Step N — [TYPE] Description
  Provider: SYSTEM | ClassName
  Rules (must ALL match):
    1. key: $.path[?(@.field == "value")] — remark: "human explanation"
    2. ...
  Action:
    type: HTTP_CALL_V2
    method: POST
    url: https://...
    body: { ... } (truncated to 500 chars)
    elseLogic: { ... } (truncated to 500 chars)
```

Also update the instruction block to explicitly ask:
- "Explain what each rule means in plain English"
- "Explain what each action does — what system it calls, what data it sends"
- Output format: use `##` headings per step, bullet lists for rules and action fields

**Prompt length concern**: each node can have large JSON payloads. Truncate `httpRequestBody` and `elseLogic` at 500 characters with a `[…truncated]` marker to keep prompt under ~6000 tokens.

### 2. Markdown rendering — use `react-markdown`

Check if `react-markdown` is already in `package.json`. If not, it is a zero-config, well-maintained library:

```bash
npm install react-markdown
```

Replace `<pre>` with:

```tsx
import ReactMarkdown from "react-markdown";

<div className="prose prose-sm max-w-none text-zinc-700">
  <ReactMarkdown>{explainResult}</ReactMarkdown>
</div>
```

**XSS safety**: `react-markdown` by default does NOT render raw HTML from the Markdown source (`rehype-raw` plugin is required to opt in). Since we are NOT adding `rehype-raw`, AI-generated HTML tags in the response are sanitized automatically. This satisfies AC-35-5.

**Tailwind prose plugin**: If `@tailwindcss/typography` is not installed, we style the container manually with Tailwind classes on child elements. Check `tailwind.config` for the `typography` plugin. If absent, apply inline styles or a minimal custom class.

**Fallback if react-markdown unavailable**: a simple regex-based renderer (headings → `<h3>`, `**bold**` → `<strong>`, `- item` → `<li>`) can serve as a no-dependency alternative. However `react-markdown` is preferred.

### 3. Check for existing dependencies

Before installing, check:
```bash
cat package.json | grep react-markdown
cat tailwind.config | grep typography
```

Decisions:
- If `react-markdown` is present → use it directly.
- If `@tailwindcss/typography` is present → add `prose` class.
- If neither → install `react-markdown` (small, ~15 kB gzipped); add basic prose styles manually.

---

## Files to Change

| File | Change |
|------|--------|
| `src/routes/workflows/-components/workflow-header/index.tsx` | Replace `buildExplainPrompt()` with richer version; replace `<pre>` with `<ReactMarkdown>` |
| `package.json` | Add `react-markdown` if not present |

---

## Security

- `react-markdown` without `rehype-raw` prevents XSS from AI-generated HTML.
- No new server endpoints.
- Token handling unchanged.

---

## Trade-offs

| Option | Pros | Cons |
|--------|------|------|
| `react-markdown` | Battle-tested, correct Markdown spec, XSS-safe by default | Small new dependency |
| Custom regex renderer | Zero new dep | Brittle; misses nested lists, code blocks, escaping |
| `dangerouslySetInnerHTML` + DOMPurify | Full HTML support | Two deps; explicit XSS surface |

**Decision:** `react-markdown` without `rehype-raw`. Smallest correct solution.

---

*Status: Draft*
