# Architecture Document — Hub Deploy & Canvas Import JSON

**Document version:** 38.0  
**Date:** 2026-04-19  
**Status:** Draft  
**Labels:** `TODO-hub-deploy-application-name`, `TODO-canvas-import-workflow-json-validate-apply`

## 1. Overview

This document describes the technical approach for two independent features:
1. **Hub Deploy** — Deploy application name and workflow to remote environment
2. **Canvas Import JSON** — Import and validate workflow JSON before applying to canvas

---

## 2. Feature 1: Hub — ApplicationName Deploy

### 2.1 User Flow

1. User clicks **Deploy** button in ApplicationName action area (desktop table row action or mobile overflow menu)
2. Modal opens with feature explanation at top
3. User fills 5-field form:
   - Deploy URL (base only, e.g., `https://workflow-operation-api-n9sbp.ondigitalocean.app`)
   - Application Name (can differ from currently loaded application)
   - Service Account Username
   - Service Account Password
   - Environment (text field for labeling, e.g., "UAT", "Production")
4. User clicks **Deploy** button
5. System executes three sequential API calls with progress indication
6. Success: green indicator, all three steps passed
7. Failure: error message indicating which step failed

### 2.2 API Call Sequence

The system auto-appends three paths to the base URL and calls in sequence:

**Step 1: Create Application Name**
```
POST {baseUrl}/workflow/entity-setting?applicationName={applicationName}
Authorization: Basic {base64(username:password)}
Content-Type: application/json

Body: {
  "enabled": true,
  "asyncMode": false,
  "retry": false,
  "tracking": false,
  "ignoreDuplicateRecordError": false
}
```

**Step 2: Update Application Name**
```
PATCH {baseUrl}/workflow/entity-setting?applicationName={applicationName}
Authorization: Basic {base64(username:password)}
Content-Type: application/json

Body: {
  "enabled": true,
  "description": "Deployed from Hub",
  // ... other settings from current application context
}
```

**Step 3: Save Workflow**
```
POST {baseUrl}/workflow?applicationName={applicationName}
Authorization: Basic {base64(username:password)}
Content-Type: application/json

Body: {
  "pluginList": [...],  // from current loaded workflow
  "uiMapList": [...]    // from current loaded workflow
}
```

### 2.3 Implementation Details

**Location:** `workflow-ui/src/components/DeployModal.tsx` (new file)

**State Management:**
```typescript
interface DeployFormData {
  baseUrl: string;
  applicationName: string;
  username: string;
  password: string;
  environment: string;
}

interface DeployProgress {
  step: 1 | 2 | 3;
  status: 'pending' | 'in-progress' | 'success' | 'error';
  message?: string;
}
```

**API Client:**
```typescript
const deployToRemote = async (
  formData: DeployFormData,
  currentWorkflow: WorkFlow,
  currentSettings: WorkflowEntitySettingRow,
  onProgress: (progress: DeployProgress) => void
): Promise<void> => {
  const auth = btoa(`${formData.username}:${formData.password}`);
  const headers = {
    'Authorization': `Basic ${auth}`,
    'Content-Type': 'application/json',
  };

  // Step 1: Create Application Name
  onProgress({ step: 1, status: 'in-progress' });
  const createResponse = await fetch(
    `${formData.baseUrl}/workflow/entity-setting?applicationName=${formData.applicationName}`,
    {
      method: 'POST',
      headers,
      body: JSON.stringify({
        enabled: true,
        asyncMode: false,
        retry: false,
        tracking: false,
        ignoreDuplicateRecordError: false,
      }),
    }
  );
  if (!createResponse.ok) {
    throw new Error(`Step 1 failed: ${await createResponse.text()}`);
  }
  onProgress({ step: 1, status: 'success' });

  // Step 2: Update Application Name
  onProgress({ step: 2, status: 'in-progress' });
  const updateResponse = await fetch(
    `${formData.baseUrl}/workflow/entity-setting?applicationName=${formData.applicationName}`,
    {
      method: 'PATCH',
      headers,
      body: JSON.stringify({
        enabled: currentSettings.enabled,
        asyncMode: currentSettings.asyncMode,
        retry: currentSettings.retry,
        tracking: currentSettings.tracking,
        ignoreDuplicateRecordError: currentSettings.ignoreDuplicateRecordError,
        eimId: currentSettings.eimId,
        defaultServiceAccount: currentSettings.defaultServiceAccount,
        region: currentSettings.region,
        retryProperties: currentSettings.retryProperties,
        description: currentSettings.description || 'Deployed from Hub',
      }),
    }
  );
  if (!updateResponse.ok) {
    throw new Error(`Step 2 failed: ${await updateResponse.text()}`);
  }
  onProgress({ step: 2, status: 'success' });

  // Step 3: Save Workflow
  onProgress({ step: 3, status: 'in-progress' });
  const saveResponse = await fetch(
    `${formData.baseUrl}/workflow?applicationName=${formData.applicationName}`,
    {
      method: 'POST',
      headers,
      body: JSON.stringify(currentWorkflow),
    }
  );
  if (!saveResponse.ok) {
    throw new Error(`Step 3 failed: ${await saveResponse.text()}`);
  }
  onProgress({ step: 3, status: 'success' });
};
```

**UI Components:**
- Carbon Modal for deploy dialog
- Three-step progress indicator (Step 1: Create → Step 2: Update → Step 3: Save)
- Form validation (all fields required, URL format validation)
- Success state: green checkmark icon with "Deployment successful" message
- Error state: red error icon with specific step failure message

**Integration Points:**
- Desktop: Add Deploy button to application table row actions (next to Settings/History/Copy/Delete)
- Mobile: Add Deploy option to overflow menu (⋯)
- Context: Deploy operates on currently loaded application (from `useWorkflowState` or application list context)

### 2.4 Error Handling

- Network timeout: 30 second timeout per API call
- Authentication failure: Clear error message "Authentication failed - check username/password"
- Step-specific failures: "Step 1 failed: Application name already exists" (example)
- Validation: URL must start with `http://` or `https://`, all fields required

### 2.5 Security Considerations

- Credentials stored only in component state during deploy operation (not persisted)
- Basic Auth over HTTPS required (warn if HTTP detected)
- No credential logging in console or error messages

---

## 3. Feature 2: Canvas — Import Workflow from JSON

### 3.1 User Flow

1. User clicks **Import** button in canvas header toolbar (desktop) or overflow menu (mobile)
2. Modal opens with:
   - Feature explanation at top
   - Textarea for pasting JSON or file upload button
   - Expandable "What belongs here?" section with example snippet
   - Note: "Not for plain English — use Generate for that"
3. User pastes JSON or uploads file
4. System validates JSON:
   - Parse JSON (strip markdown code fences if present)
   - Validate against WorkFlow schema
   - Show human-readable errors with line/path hints
5. If valid: Show preview summary (node count, warnings)
6. User clicks **Apply**
7. If canvas non-empty: Confirmation prompt "Replace current workflow?"
8. Apply replaces canvas workflow
9. User can still click **Save** to persist to backend

### 3.2 Validation Rules

**JSON Parsing:**
- Strip markdown code fences: ` ```json ... ``` ` or ` ``` ... ``` `
- Parse as JSON
- If parse fails: Show error with line number

**Schema Validation:**
```typescript
interface WorkFlowValidation {
  valid: boolean;
  errors: ValidationError[];
  warnings: ValidationWarning[];
  summary: {
    nodeCount: number;
    edgeCount: number;
  };
}

interface ValidationError {
  path: string;  // e.g., "pluginList[2].action.type"
  message: string;
  line?: number;
}

const validateWorkflow = (json: any): WorkFlowValidation => {
  const errors: ValidationError[] = [];
  const warnings: ValidationWarning[] = [];

  // Required fields
  if (!json.pluginList) {
    errors.push({ path: 'pluginList', message: 'Required field missing' });
  }
  if (!json.uiMapList) {
    errors.push({ path: 'uiMapList', message: 'Required field missing' });
  }

  // Validate pluginList
  if (Array.isArray(json.pluginList)) {
    json.pluginList.forEach((plugin: any, index: number) => {
      // Unique IDs
      if (!plugin.id) {
        errors.push({
          path: `pluginList[${index}].id`,
          message: 'Plugin ID required',
        });
      }

      // Valid plugin types
      if (plugin.action?.type) {
        const validTypes = ['FUNCTION_V2', 'FUNCTION_V3', 'HTTP_CALL', 'LOGIC'];
        if (!validTypes.includes(plugin.action.type)) {
          errors.push({
            path: `pluginList[${index}].action.type`,
            message: `Invalid plugin type: ${plugin.action.type}`,
          });
        }
      }

      // Rule validation
      if (Array.isArray(plugin.ruleList)) {
        plugin.ruleList.forEach((rule: any, ruleIndex: number) => {
          if (!rule.key) {
            errors.push({
              path: `pluginList[${index}].ruleList[${ruleIndex}].key`,
              message: 'Rule key required',
            });
          }
        });
      }
    });

    // Check for duplicate IDs
    const ids = json.pluginList.map((p: any) => p.id).filter(Boolean);
    const duplicates = ids.filter((id: any, i: number) => ids.indexOf(id) !== i);
    if (duplicates.length > 0) {
      errors.push({
        path: 'pluginList',
        message: `Duplicate plugin IDs: ${duplicates.join(', ')}`,
      });
    }
  }

  // Validate uiMapList edges
  if (Array.isArray(json.uiMapList)) {
    json.uiMapList.forEach((edge: any, index: number) => {
      if (edge.source && !json.pluginList?.find((p: any) => p.id === edge.source)) {
        errors.push({
          path: `uiMapList[${index}].source`,
          message: `Edge source "${edge.source}" does not exist in pluginList`,
        });
      }
      if (edge.target && !json.pluginList?.find((p: any) => p.id === edge.target)) {
        errors.push({
          path: `uiMapList[${index}].target`,
          message: `Edge target "${edge.target}" does not exist in pluginList`,
        });
      }
    });
  }

  return {
    valid: errors.length === 0,
    errors,
    warnings,
    summary: {
      nodeCount: json.pluginList?.length || 0,
      edgeCount: json.uiMapList?.length || 0,
    },
  };
};
```

### 3.3 Implementation Details

**Location:** `workflow-ui/src/components/ImportWorkflowModal.tsx` (new file)

**UI Components:**
- Carbon Modal
- Textarea with syntax highlighting (optional)
- File upload button (accepts `.json` files)
- Validation error list with expandable details
- Preview summary card
- Apply button (disabled if validation errors present)
- Confirmation dialog if canvas non-empty

**Integration Points:**
- Desktop: Add Import button to canvas header toolbar (next to Explain/Generate/Run)
- Mobile: Add Import option to canvas overflow menu (⋯)
- Canvas state: Use `useWorkflowState` to get/set workflow
- Confirmation: Use Carbon Modal.confirm for non-empty canvas warning

**Example JSON Snippet (shown in "What belongs here?" section):**
```json
{
  "pluginList": [
    {
      "id": 1,
      "description": "Validate input",
      "linkingIdOfRuleListAndAction": "rule-1",
      "ruleList": [
        { "key": "$.data.amount", "remark": "Amount exists" }
      ],
      "action": {
        "type": "FUNCTION_V2",
        "provider": "validation",
        "remark": "Check amount"
      }
    }
  ],
  "uiMapList": [
    { "source": "1", "target": "2" }
  ]
}
```

### 3.4 Error Messages

**Parse Errors:**
- "Invalid JSON at line 15: Unexpected token }"
- "Invalid JSON: Expected property name or '}'"

**Validation Errors:**
- "pluginList[2].id: Plugin ID required"
- "pluginList[0].action.type: Invalid plugin type 'CUSTOM' (must be FUNCTION_V2, FUNCTION_V3, HTTP_CALL, or LOGIC)"
- "uiMapList[3].target: Edge target 'node-99' does not exist in pluginList"
- "pluginList: Duplicate plugin IDs: 1, 3"

**Warnings:**
- "Node 5 has no incoming or outgoing edges (orphaned node)"
- "Large workflow: 150 nodes may impact canvas performance"

### 3.5 Differences from Generate

| Feature | Import JSON | Generate (AI) |
|---------|-------------|---------------|
| Input | Valid WorkFlow JSON | Natural language description |
| Validation | Client-side schema check | AI output + schema check |
| Use case | Hand-edited or external JSON | Create workflow from scratch |
| Error handling | Detailed line/path hints | Retry with improved prompt |
| Modal location | Canvas toolbar | Canvas toolbar |

---

## 4. Testing Strategy

### 4.1 Deploy Feature

**Unit Tests:**
- Form validation (URL format, required fields)
- API call sequence logic
- Error handling for each step
- Progress state transitions

**Integration Tests:**
- Deploy to UAT environment with valid credentials
- Deploy with invalid credentials (expect auth failure)
- Deploy with existing application name (expect Step 1 failure)
- Deploy with network timeout (expect timeout error)

**E2E Tests (Playwright):**
- TC-DEPLOY-01: Desktop deploy flow (fill form, verify 3-step progress, verify success)
- TC-DEPLOY-02: Mobile deploy flow (overflow menu, same validation)
- TC-DEPLOY-03: Deploy to different application name (UAT key test case)
- TC-DEPLOY-04: Deploy with invalid URL (expect validation error)
- TC-DEPLOY-05: Deploy with auth failure (expect clear error message)

### 4.2 Import JSON Feature

**Unit Tests:**
- JSON parsing (with/without code fences)
- Schema validation (all error cases)
- Edge validation (source/target existence)
- Duplicate ID detection

**Integration Tests:**
- Import valid workflow JSON (verify canvas updated)
- Import invalid JSON (verify error messages)
- Import with missing required fields (verify specific errors)
- Import with duplicate IDs (verify error)
- Import with orphaned nodes (verify warning)

**E2E Tests (Playwright):**
- TC-IMPORT-01: Desktop import valid JSON (paste, validate, apply, verify canvas)
- TC-IMPORT-02: Mobile import valid JSON (file upload, same flow)
- TC-IMPORT-03: Import invalid JSON (verify error display)
- TC-IMPORT-04: Import with non-empty canvas (verify confirmation prompt)
- TC-IMPORT-05: Import with markdown code fences (verify stripped correctly)

---

## 5. Non-Functional Requirements

### 5.1 Performance
- Deploy: Each API call timeout 30s (total max 90s)
- Import validation: Complete within 2s for workflows up to 100 nodes
- Import validation: Complete within 5s for workflows up to 500 nodes

### 5.2 Security
- Deploy credentials never logged or persisted
- Deploy requires HTTPS (warn on HTTP)
- Import validation runs client-side (no server upload of potentially malicious JSON)

### 5.3 Accessibility
- Both modals WCAG 2.1 AA compliant
- Keyboard navigation for all form fields
- Screen reader announcements for progress updates
- Error messages associated with form fields via aria-describedby

### 5.4 Carbon Design System Compliance
- Both modals use Carbon Modal component
- Form inputs use Carbon TextInput/TextArea
- Buttons use Carbon Button (primary/secondary)
- Progress indicator uses Carbon ProgressIndicator
- Error messages use Carbon InlineNotification
- All components: 0px border-radius, IBM Plex fonts, Blue 60 primary color

---

## 6. Deployment Plan

### 6.1 Deploy Feature
1. Implement `DeployModal.tsx` component
2. Add Deploy button to desktop application table
3. Add Deploy option to mobile overflow menu
4. Write unit tests
5. Write E2E tests
6. QA verification
7. UAT on https://workflow-ui-gamma.vercel.app

### 6.2 Import JSON Feature
1. Implement `ImportWorkflowModal.tsx` component
2. Implement validation logic
3. Add Import button to canvas toolbar
4. Add Import option to mobile overflow menu
5. Write unit tests
6. Write E2E tests
7. QA verification
8. UAT on https://workflow-ui-gamma.vercel.app

### 6.3 Rollout
- Both features are additive (no breaking changes)
- No backend changes required
- No database migrations required
- Deploy to UAT first, verify, then production

---

## 7. Open Questions

1. **Deploy:** Should we persist deploy history (which environments were deployed to)?
   - **Decision:** No, out of scope for v38.0. Deploy is one-shot operation.

2. **Deploy:** Should we validate that target environment is reachable before starting deploy?
   - **Decision:** No, let first API call fail naturally. Adds complexity for minimal benefit.

3. **Import:** Should we support importing from URL (not just paste/upload)?
   - **Decision:** No, out of scope for v38.0. Paste/upload covers primary use cases.

4. **Import:** Should we auto-save after successful import?
   - **Decision:** No, user must explicitly click Save. Import is preview/apply, not persist.

---

*End of Architecture Document v38.0*
