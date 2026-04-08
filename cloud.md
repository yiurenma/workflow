# Cloud agent — workflow constraints

Rules for agents running in cloud / Cursor environments in addition to `CLAUDE.md` and `workflow-agent-teams/`.

---

## PM master document (mandatory for every TODO)

**Canonical file (fixed name):** `workflow-agent-teams/docs/pm-doc-master.md`

For **every** item picked up from `workflow-agent-teams/TODO.md` (new feature, bug fix, post-mortem, doc-only task, or infra task that changes product-visible behavior):

1. **PM must update** `pm-doc-master.md` in the **same change set** as the PM doc slice for that item (or as the sole PM update if the slice is folded into the master).
2. **Bump the document version** inside `pm-doc-master.md` (e.g. `2.5` → `2.6`). The **filename stays** `pm-doc-master.md`.
3. **Reflect the TODO in the master:** add or adjust **user stories and/or acceptance criteria** so the master remains the single product baseline. Map work to domain prefixes **APP** (application management), **REC** (execution records / online semantics), **CV** (canvas), as used in the master.
4. **Record traceability:** in the master’s **Revision history** (or **Changelog**) table, add a row: version, date, `TODO.md` label or title, and which **US/AC** IDs changed or were added.

**PM ownership:** Treat maintenance of `pm-doc-master.md` as a standing PM deliverable—same priority as writing `pm-doc-vX.Y.md` when the process still requires a versioned slice for Architect/Test alignment.

---

## English / 中文

- The master document **must** contain **both** a full **English** section and a full **中文** section (same structure and equivalent requirements). Updating one without the other is incomplete.
- When a TODO is processed, **both** language sections must be updated and the **same** document version applies to both.

---

## 每个 TODO 必须更新主需求文档（中文摘要）

- **固定文件名：** `workflow-agent-teams/docs/pm-doc-master.md`（不改名）。
- **每个** `TODO.md` 条目：产品经理须 **同步更新** 该文件，**递增文档版本号**，并在正文中体现对应用户故事与验收标准（**APP / REC / CV** 编号体系）。
- **修订记录** 中写明：版本、日期、TODO 标签或标题、涉及的 **US/AC**。
- **英文与中文** 两部分须 **同时** 更新，版本号一致。
