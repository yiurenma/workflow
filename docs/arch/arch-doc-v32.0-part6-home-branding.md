# Architecture — Workflow Studio Unified Program Part 6: Home & Branding (v32.0)

**Document version:** 32.0 Part 6  
**Date:** 2026-04-12  
**Status:** Draft — awaiting human approval  
**Scope:** Home screen introduction + snail favicon  
**Repository:** `workflow-ui`

---

## 1. Technical Context

### 1.1 Current Home Screen

**File:** `workflow-ui/src/routes/workflows/index.tsx` — Applications list

**Current behavior:**
- Shows applications table (desktop) or cards (mobile)
- No introduction or onboarding content
- New users land directly on empty list or existing apps

**Gap:** No context for what the product is or how to use it.

---

### 1.2 Current Favicon

**File:** `workflow-ui/public/favicon.ico` (or equivalent)

**Current state:** Generic or missing favicon.

**Gap:** No branded icon in browser tab.

---

## 2. Solution Design

### 2.1 Home Screen Introduction

**Approach:** Add dismissible hero section above applications list.

**Implementation:**

```tsx
// routes/workflows/index.tsx
import { useState, useEffect } from 'react';
import { Alert, Button } from 'antd';

const WorkflowsHome = () => {
  const [showIntro, setShowIntro] = useState(true);

  useEffect(() => {
    const dismissed = localStorage.getItem('workflow_intro_dismissed');
    if (dismissed === 'true') {
      setShowIntro(false);
    }
  }, []);

  const handleDismiss = () => {
    localStorage.setItem('workflow_intro_dismissed', 'true');
    setShowIntro(false);
  };

  return (
    <div className="workflows-home">
      {showIntro && (
        <div className="intro-section bg-[--cds-layer-01] p-8 mb-6 rounded">
          <div className="flex justify-between items-start">
            <div className="flex-1">
              <h1 className="text-3xl font-bold mb-4">Welcome to Workflow Studio</h1>
              
              <p className="text-lg mb-4">
                Workflow Studio enables configurable message enrichment, conditional logic, 
                and multi-channel delivery without writing custom backend code.
              </p>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
                <div>
                  <h3 className="font-semibold mb-2">Who it's for</h3>
                  <ul className="list-disc list-inside text-sm">
                    <li>Integration engineers</li>
                    <li>Application developers</li>
                    <li>QA and operations teams</li>
                    <li>Business analysts</li>
                  </ul>
                </div>

                <div>
                  <h3 className="font-semibold mb-2">Key benefits</h3>
                  <ul className="list-disc list-inside text-sm">
                    <li>Speed vs code-only integration</li>
                    <li>Shared business/engineering view</li>
                    <li>Rules as guardrails (JSONPath)</li>
                    <li>Records as observability</li>
                  </ul>
                </div>
              </div>

              <div>
                <h3 className="font-semibold mb-2">Features</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-2 text-sm">
                  <div>📋 Applications</div>
                  <div>🎨 Canvas Editor</div>
                  <div>▶️ Run & Test</div>
                  <div>📊 Records</div>
                  <div>💡 AI Explain</div>
                  <div>✨ AI Generate</div>
                  <div>🔍 JsonPath Tool</div>
                  <div>📱 Mobile UI</div>
                </div>
              </div>
            </div>

            <Button
              type="text"
              onClick={handleDismiss}
              className="ml-4"
            >
              Dismiss
            </Button>
          </div>
        </div>
      )}

      {/* Existing applications list */}
      <ApplicationsList />
    </div>
  );
};
```

**Alternative: Modal on first visit**

```tsx
const [showIntroModal, setShowIntroModal] = useState(false);

useEffect(() => {
  const visited = localStorage.getItem('workflow_visited');
  if (!visited) {
    setShowIntroModal(true);
    localStorage.setItem('workflow_visited', 'true');
  }
}, []);

<Modal
  open={showIntroModal}
  onCancel={() => setShowIntroModal(false)}
  footer={[
    <Button key="close" type="primary" onClick={() => setShowIntroModal(false)}>
      Get Started
    </Button>
  ]}
  width={800}
>
  {/* Same intro content as above */}
</Modal>
```

---

### 2.2 Snail Favicon

**Approach:** Generate snail icon in multiple formats; wire via `index.html`.

**Step 1: Generate icon assets**

Use design tool (Figma, Sketch) or AI image generator to create snail icon:
- Simple, recognizable silhouette
- Readable at 16×16 and 32×32
- Color: Carbon Gray 80 `#393939` or terracotta `#7C4A3A` (brand exception)

**Export formats:**
- `favicon.svg` — vector, scalable
- `favicon-16x16.png` — 16×16 PNG
- `favicon-32x32.png` — 32×32 PNG
- `apple-touch-icon.png` — 180×180 PNG (iOS home screen)
- `icon-192x192.png` — 192×192 PNG (Android, PWA)
- `icon-512x512.png` — 512×512 PNG (PWA splash screen)
- `favicon.ico` — multi-size ICO (16×16, 32×32, 48×48 for legacy browsers)

**Step 2: Add to public directory**

```
workflow-ui/public/
  favicon.svg
  favicon-16x16.png
  favicon-32x32.png
  apple-touch-icon.png
  icon-192x192.png
  icon-512x512.png
  favicon.ico
```

**Step 3: Update index.html**

```html
<!-- workflow-ui/index.html -->
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Workflow Studio</title>

  <!-- Favicon -->
  <link rel="icon" type="image/svg+xml" href="/favicon.svg">
  <link rel="icon" type="image/png" sizes="32x32" href="/favicon-32x32.png">
  <link rel="icon" type="image/png" sizes="16x16" href="/favicon-16x16.png">
  <link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png">
  <link rel="shortcut icon" href="/favicon.ico">

  <!-- PWA manifest (if exists) -->
  <link rel="manifest" href="/manifest.json">
</head>
<body>
  <div id="root"></div>
  <script type="module" src="/src/main.tsx"></script>
</body>
</html>
```

**Step 4: Update PWA manifest (if exists)**

```json
// workflow-ui/public/manifest.json
{
  "name": "Workflow Studio",
  "short_name": "Workflow",
  "description": "Visual workflow editor for message enrichment and routing",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#0f62fe",
  "icons": [
    {
      "src": "/icon-192x192.png",
      "sizes": "192x192",
      "type": "image/png"
    },
    {
      "src": "/icon-512x512.png",
      "sizes": "512x512",
      "type": "image/png"
    }
  ]
}
```

---

## 3. Implementation Plan

### Phase 1: Home Screen Introduction (2-3 hours)

1. Draft intro copy (what/who/benefits/features)
2. Implement hero section or modal component
3. Add localStorage persistence for dismiss state
4. Test: verify intro shows on first visit, dismisses, doesn't show again
5. Test: verify accessible contrast (WCAG AA)

---

### Phase 2: Snail Favicon (2-3 hours)

1. Generate snail icon assets (or commission from designer)
2. Export all required formats (SVG, PNG, ICO)
3. Add assets to `public/` directory
4. Update `index.html` with `<link>` tags
5. Update `manifest.json` (if exists)
6. Test: verify favicon visible in browser tab (Chrome, Safari, Firefox, Mobile)

---

## 4. Testing Strategy

### 4.1 Manual Testing

- **Home intro:**
  - First visit: verify intro visible
  - Dismiss: verify intro hidden
  - Refresh: verify intro stays hidden
  - Clear localStorage: verify intro reappears

- **Favicon:**
  - Desktop Chrome: verify snail icon in tab
  - Desktop Safari: verify snail icon in tab
  - Desktop Firefox: verify snail icon in tab
  - Mobile Chrome: verify snail icon in tab
  - Mobile Safari: verify snail icon in tab
  - iOS home screen: verify apple-touch-icon used

### 4.2 Playwright Testing

- Optional: Add test case for intro visibility/dismiss
- Verify existing tests still pass

---

## 5. Risks & Mitigations

**Risk:** Intro too verbose; users dismiss without reading.  
**Mitigation:** Keep copy concise; use bullet points; test with real users.

**Risk:** Favicon not visible on some browsers/devices.  
**Mitigation:** Test on all major browsers; provide fallback ICO format.

**Risk:** Intro blocks access to applications list.  
**Mitigation:** Make intro dismissible; consider modal instead of hero section.

---

## 6. Deployment

- **Branch:** `main` (workflow-ui)
- **Merge strategy:** One commit for intro, one commit for favicon
- **Rollback plan:** Revert individual commits if issues detected

---

## 7. References

- Favicon best practices: https://evilmartians.com/chronicles/how-to-favicon-in-2021
- PWA manifest spec: https://developer.mozilla.org/en-US/docs/Web/Manifest
- WCAG contrast checker: https://webaim.org/resources/contrastchecker/
