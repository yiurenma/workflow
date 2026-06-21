# Architecture Document v32.0

**Document version:** 32.0  
**Date:** 2026-04-12  
**Status:** Draft  
**Recognition label:** `TODO-branding-favicon-snail-tab-icon`

## Overview

Technical approach for adding snail favicon to Workflow Studio.

## Favicon Formats

### SVG (Modern browsers)
- Vector format, scales perfectly
- Single file for all resolutions
- Supported by Chrome 80+, Firefox 41+, Safari 9+

### ICO (Legacy browsers)
- Contains 16×16 and 32×32 bitmaps
- Fallback for older browsers
- Required for IE11 and older

### PNG (PWA and mobile)
- 192×192: minimum for PWA manifest
- 512×512: high-res for splash screens
- apple-touch-icon: 180×180 for iOS home screen

## Implementation

### Step 1: Create SVG favicon

Simple snail silhouette in SVG:

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32">
  <path fill="#0f62fe" d="M16,8 C12,8 10,10 10,14 C10,18 12,20 16,20 C20,20 22,18 22,14 C22,10 20,8 16,8 Z M16,10 C18,10 20,11 20,14 C20,17 18,18 16,18 C14,18 12,17 12,14 C12,11 14,10 16,10 Z M8,16 Q8,12 12,12 L12,14 Q10,14 10,16 L10,20 Q10,22 12,22 L20,22 Q22,22 22,20 L22,16 Q22,14 20,14 L20,12 Q24,12 24,16 L24,20 Q24,24 20,24 L12,24 Q8,24 8,20 Z"/>
</svg>
```

### Step 2: Generate ICO file

Use online tool or ImageMagick:
```bash
convert favicon.svg -define icon:auto-resize=32,16 favicon.ico
```

### Step 3: Generate PNG files

```bash
convert favicon.svg -resize 192x192 favicon-192.png
convert favicon.svg -resize 512x512 favicon-512.png
convert favicon.svg -resize 180x180 apple-touch-icon.png
```

### Step 4: Update index.html

Add to `<head>`:

```html
<!-- Favicon -->
<link rel="icon" type="image/svg+xml" href="/favicon.svg">
<link rel="icon" type="image/x-icon" href="/favicon.ico">
<link rel="apple-touch-icon" href="/apple-touch-icon.png">
```

### Step 5: Update PWA manifest (if exists)

Add to `manifest.json`:

```json
{
  "icons": [
    {
      "src": "/favicon-192.png",
      "sizes": "192x192",
      "type": "image/png",
      "purpose": "any maskable"
    },
    {
      "src": "/favicon-512.png",
      "sizes": "512x512",
      "type": "image/png",
      "purpose": "any maskable"
    }
  ]
}
```

## File Locations

- `workflow-ui/public/favicon.svg`
- `workflow-ui/public/favicon.ico`
- `workflow-ui/public/favicon-192.png`
- `workflow-ui/public/favicon-512.png`
- `workflow-ui/public/apple-touch-icon.png`
- `workflow-ui/index.html` (update `<head>`)
- `workflow-ui/public/manifest.json` (update if exists)

## Testing

1. Local dev server: verify favicon appears in tab
2. Build and deploy: verify favicon on UAT
3. Test in multiple browsers: Chrome, Firefox, Safari
4. Test on mobile: iOS Safari, Android Chrome
5. Test bookmark: verify icon appears in bookmark list

## Success Criteria

- Favicon visible in browser tab at 16×16
- Favicon visible in bookmarks
- No console errors
- Icon recognizable as snail
