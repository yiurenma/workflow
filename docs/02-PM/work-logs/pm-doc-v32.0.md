# Product Requirements Document v32.0

**Document version:** 32.0  
**Date:** 2026-04-12  
**Status:** Draft  
**Recognition label:** `TODO-branding-favicon-snail-tab-icon`

## Overview

Add snail icon as favicon for Workflow Studio to improve brand recognition in browser tabs and bookmarks.

## Problem Statement

Current application has no custom favicon, showing default browser icon in tabs and bookmarks. This makes it difficult for users to:
- Quickly identify Workflow Studio among multiple open tabs
- Recognize the application in browser bookmarks
- Associate the application with its brand identity

## User Stories

**US-32.1:** As a workflow editor user, I want to see a snail icon in my browser tab, so that I can quickly identify Workflow Studio among my open tabs.

**US-32.2:** As a workflow editor user, I want to see a snail icon in my bookmarks, so that I can easily recognize Workflow Studio in my bookmark list.

**US-32.3:** As a mobile user, I want to see a snail icon when I add Workflow Studio to my home screen, so that the app has a recognizable icon.

## Acceptance Criteria

**AC-32.1:** Favicon files created
- SVG favicon for modern browsers
- `favicon.ico` with 16×16 and 32×32 sizes for legacy browsers
- PNG sizes: 192×192 and 512×512 for PWA manifest

**AC-32.2:** Favicon wired in HTML
- `<link rel="icon" type="image/svg+xml" href="/favicon.svg">` in index.html
- `<link rel="icon" type="image/x-icon" href="/favicon.ico">` fallback
- `<link rel="apple-touch-icon" href="/apple-touch-icon.png">` for iOS

**AC-32.3:** Icon quality
- Readable at 16×16 and 32×32 sizes
- Aligns with Carbon design system neutrals OR documented brand exception
- Simple, recognizable snail silhouette

**AC-32.4:** PWA manifest (if exists)
- Icons array includes 192×192 and 512×512 PNG
- Purpose: "any maskable" for adaptive icons

## Scope

**In scope:**
- Create snail icon in multiple formats (SVG, ICO, PNG)
- Wire favicon in index.html
- Add apple-touch-icon for iOS
- Update PWA manifest if it exists

**Out of scope:**
- Changing application name or branding
- Adding other brand assets (logo, splash screen)
- Backend changes

## References

- TODO.md §10: Branding snail favicon
- Carbon Design System color tokens

## Success Metrics

- Favicon visible in browser tab
- Favicon visible in bookmarks
- Icon readable at small sizes (16×16)
- No console errors related to favicon loading
