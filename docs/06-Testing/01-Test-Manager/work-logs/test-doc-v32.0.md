# Test Document v32.0

**Document version:** 32.0  
**Date:** 2026-04-12  
**Status:** Draft  
**Recognition label:** `TODO-branding-favicon-snail-tab-icon`

## Test Scope

Test snail favicon visibility and quality across browsers and devices.

## Test Cases

### TC-FAVICON-01: Favicon visible in browser tab (Desktop Chrome)

**Preconditions:**
- Desktop Chrome browser
- Navigate to http://localhost:5173 or UAT URL

**Steps:**
1. Open application in browser
2. Observe browser tab

**Expected Result:**
- Snail icon visible in tab
- Icon recognizable at small size
- No broken image icon

**Acceptance Criteria:** AC-32.1, AC-32.2

---

### TC-FAVICON-02: Favicon visible in bookmarks

**Preconditions:**
- Desktop Chrome browser
- Application open

**Steps:**
1. Bookmark the application (Cmd+D / Ctrl+D)
2. Open bookmarks bar or bookmarks manager
3. Observe bookmark icon

**Expected Result:**
- Snail icon visible next to bookmark
- Icon recognizable

**Acceptance Criteria:** AC-32.1, AC-32.2

---

### TC-FAVICON-03: Favicon readable at 16×16

**Preconditions:**
- Desktop browser with developer tools

**Steps:**
1. Open application
2. Open developer tools
3. Inspect favicon in Network tab or Elements tab
4. Verify favicon dimensions

**Expected Result:**
- Favicon renders clearly at 16×16 pixels
- Snail shape recognizable
- No pixelation or blur

**Acceptance Criteria:** AC-32.3

---

### TC-FAVICON-04: Apple touch icon on iOS

**Preconditions:**
- iOS Safari browser
- Application open

**Steps:**
1. Open application in Safari
2. Tap Share button
3. Tap "Add to Home Screen"
4. Observe icon preview

**Expected Result:**
- Snail icon visible in preview
- Icon quality good (180×180)
- Icon appears on home screen after adding

**Acceptance Criteria:** AC-32.2, AC-32.3

---

### TC-FAVICON-05: No console errors

**Preconditions:**
- Desktop browser with developer tools

**Steps:**
1. Open application
2. Open developer tools Console tab
3. Observe console for errors

**Expected Result:**
- No 404 errors for favicon files
- No console warnings about favicon
- All favicon formats load successfully

**Acceptance Criteria:** AC-32.2

---

### TC-FAVICON-06: Multiple browser support

**Preconditions:**
- Access to Chrome, Firefox, Safari

**Steps:**
1. Open application in Chrome
2. Open application in Firefox
3. Open application in Safari
4. Observe favicon in each browser tab

**Expected Result:**
- Favicon visible in all three browsers
- Icon quality consistent across browsers
- No broken images

**Acceptance Criteria:** AC-32.1, AC-32.2

---

## Manual Testing Checklist

- [ ] Desktop Chrome: favicon visible in tab
- [ ] Desktop Firefox: favicon visible in tab
- [ ] Desktop Safari: favicon visible in tab
- [ ] Bookmarks: favicon visible
- [ ] iOS Safari: apple-touch-icon works
- [ ] Android Chrome: favicon visible
- [ ] No console errors for favicon files
- [ ] Icon readable at 16×16
- [ ] Icon readable at 32×32
- [ ] PWA manifest icons (if applicable)

## Success Criteria

- ✅ All test cases pass
- ✅ Favicon visible in all tested browsers
- ✅ No console errors
- ✅ Icon recognizable at small sizes
