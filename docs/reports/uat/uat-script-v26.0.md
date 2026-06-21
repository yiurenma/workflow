# UAT Script v26.0

**Document version:** 26.0  
**Date:** 2026-04-11  
**Status:** Ready for Execution  
**TODO label:** `TODO-vercel-build-missing-dayjs-dependency`

## Prerequisites

- Vercel deployment completed successfully
- UAT environment accessible: https://workflow-ui-gamma.vercel.app
- Browser: Chrome/Firefox/Safari (latest version)
- Test data: At least one workflow application and execution record exists

## UAT Test Steps

### Test 1: Verify Vercel Deployment Success

**Objective:** Confirm the build completed without dayjs import errors

**Steps:**
1. Open Vercel dashboard: https://vercel.com/dashboard
2. Navigate to workflow-ui project
3. Check latest deployment status
4. Review build logs for any errors

**Expected Result:**
- Deployment status: "Ready" (green checkmark)
- No "failed to resolve import 'dayjs'" errors in logs
- Build completed successfully

**Pass/Fail:** _________

---

### Test 2: Records Page - Date Picker Functionality

**Objective:** Verify dayjs is working correctly for date formatting

**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app/records
2. Locate the date filter section (From / To fields)
3. Click on "From" DatePicker
4. Select date: 2026-04-01
5. Verify the selected date displays correctly
6. Click on "To" DatePicker
7. Select date: 2026-04-11
8. Verify the selected date displays correctly
9. Click "Search" button
10. Verify records table/cards load without errors
11. Open browser DevTools Console (F12)
12. Check for any JavaScript errors related to dayjs

**Expected Result:**
- DatePicker opens and displays calendar
- Selected dates show in correct format
- Search executes successfully
- Records filter by date range
- No console errors mentioning "dayjs" or "failed to resolve import"

**Pass/Fail:** _________

**Notes:** _________________________________________

---

### Test 3: Records Page - Mobile Viewport

**Objective:** Verify date picker works on mobile viewport

**Steps:**
1. Open browser DevTools (F12)
2. Toggle device toolbar (mobile view)
3. Set viewport to iPhone (390px width)
4. Navigate to https://workflow-ui-gamma.vercel.app/records
5. Tap "More filters" to expand date filters
6. Tap "From" DatePicker
7. Select a date using mobile date picker
8. Verify date displays correctly
9. Repeat for "To" DatePicker
10. Apply filters

**Expected Result:**
- Mobile date picker renders correctly
- Touch interactions work smoothly
- Date selection functional on mobile
- No layout issues

**Pass/Fail:** _________

**Notes:** _________________________________________

---

### Test 4: Canvas Editor - No Regression

**Objective:** Verify @xyflow/react still works (no dependency conflicts)

**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app
2. Click on any workflow application to open canvas
3. Verify canvas renders with nodes and edges visible
4. Drag a node to a new position
5. Verify node moves smoothly
6. Click "Add Node" button (desktop) or FAB (mobile)
7. Verify Add Node sheet/modal opens
8. Add a new HTTP Call node
9. Verify node appears on canvas
10. Click on the new node to open node editor
11. Verify node editor drawer opens
12. Check browser console for errors

**Expected Result:**
- Canvas renders correctly
- All node interactions work (drag, select, add)
- Node editor opens without errors
- No console errors related to @xyflow/react
- No regression in canvas functionality

**Pass/Fail:** _________

**Notes:** _________________________________________

---

### Test 5: Application List Page

**Objective:** Verify no regression on other pages

**Steps:**
1. Navigate to https://workflow-ui-gamma.vercel.app
2. Verify application list loads
3. Verify application cards/table display correctly
4. Click on an application
5. Verify navigation to canvas works
6. Go back to application list
7. Check browser console for errors

**Expected Result:**
- Application list loads successfully
- All UI elements render correctly
- Navigation works smoothly
- No console errors

**Pass/Fail:** _________

**Notes:** _________________________________________

---

## Summary

| Test | Pass/Fail | Notes |
|------|-----------|-------|
| 1. Vercel Deployment | | |
| 2. Records Date Picker | | |
| 3. Mobile Date Picker | | |
| 4. Canvas Editor | | |
| 5. Application List | | |

**Overall Result:** PASS / FAIL

**Tested By:** _________________________________________

**Date/Time:** _________________________________________

**Additional Notes:**
_________________________________________
_________________________________________
_________________________________________

---

## Next Steps After UAT

If all tests PASS:
1. Update ui-test-report-v26.0.md with UAT results
2. Create uat-report-v26.0.md documenting all test results
3. Mark TODO item as Done in workflow-agent-teams/TODO.md
4. Commit and push documentation updates

If any test FAILS:
1. Document failure details in uat-report-v26.0.md
2. Create new TODO item for the failure
3. Do NOT mark original TODO as Done
4. Investigate and fix the issue
