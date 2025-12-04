# Firebase Cloud Functions Changelog

## [2.0.0] - 2025-12-04

### 🚀 Major Migration: v1 → v2 Functions

#### Breaking Changes
- **Migrated from Firebase Functions v1 to v2**: All callable functions now use the v2 `onCall` API
- **Replaced `functions.config()` with Secret Manager**: Gemini API key is now managed via Firebase Secret Manager using `defineSecret`
- **Updated function signatures**: Functions now receive API key via parameter injection instead of reading from config

#### Changes

##### Infrastructure
- ✅ Migrated `readCupCallable` to v2 `onCall` format
- ✅ Migrated `getDailyReadingCallable` to v2 `onCall` format
- ✅ Replaced deprecated `functions.config()` API with `defineSecret("GEMINI_API_KEY")`
- ✅ Updated function configuration:
  - Memory: `1GB` → `1GiB` (v2 format)
  - Region: Explicitly set to `us-central1`
  - Timeout: 60 seconds (unchanged)
  - Min/Max instances: 0-10 (unchanged)

##### Code Refactoring
- ✅ **`index.ts`**: 
  - Migrated to `firebase-functions/v2/https` imports
  - Added `defineSecret` for `GEMINI_API_KEY`
  - Updated function handlers to use v2 request format
  - Maintained backward compatibility by converting v2 request to v1-style context

- ✅ **`gemini.ts`**:
  - Removed `getGeminiApiKey()` function that used `functions.config()`
  - Updated `generateReadingWithGemini()` to accept `apiKey` as first parameter
  - Removed dependency on deprecated config API

- ✅ **`readCup.ts`**:
  - Added `geminiApiKey: string` parameter
  - Updated to pass API key to `generateReadingWithGemini()`
  - No changes to core business logic

- ✅ **`dailyReading.ts`**:
  - Removed `getGeminiApiKey()` function
  - Added `geminiApiKey: string` parameter
  - Updated to use injected API key directly
  - No changes to core business logic

##### Security
- ✅ **Secret Management**: 
  - Created `GEMINI_API_KEY` secret in Firebase Secret Manager
  - Migrated existing API key from `functions.config()` to Secret Manager
  - Functions now use secure parameter injection instead of environment variables

##### Deployment
- ✅ Deleted old v1 functions before deploying v2 versions
- ✅ Successfully deployed both functions as 2nd Gen Cloud Functions
- ✅ Verified secret access permissions for compute service account

#### Migration Notes

**Before (v1)**:
```typescript
export const readCupCallable = functions
  .https.onCall(async (data, context) => {
    // Used functions.config() internally
    return readCup(data, context);
  });
```

**After (v2)**:
```typescript
const geminiApiKey = defineSecret("GEMINI_API_KEY");

export const readCupCallable = onCall(
  {
    secrets: [geminiApiKey],
    timeoutSeconds: 60,
    memory: "1GiB",
    region: "us-central1",
  },
  async (request) => {
    return readCup(request.data, context, geminiApiKey.value());
  }
);
```

#### Benefits
- ✅ **Future-proof**: No dependency on deprecated `functions.config()` API
- ✅ **Better security**: Secrets managed through Firebase Secret Manager
- ✅ **Improved performance**: v2 functions have better cold start times
- ✅ **Better observability**: Enhanced logging and monitoring in v2
- ✅ **Type safety**: Better TypeScript support in v2 API

#### Compatibility
- ✅ **Backward compatible**: Android app continues to work without changes
- ✅ **API contract unchanged**: Function signatures and response formats remain the same
- ✅ **No client-side changes required**: Migration is transparent to clients

#### Next Steps
- ⚠️ Consider removing old `functions.config()` data after verifying v2 functions work correctly
- 📝 Update documentation to reflect v2 function usage
- 🔄 Monitor function performance and adjust scaling if needed

---

## Previous Versions

### [1.0.0] - Initial Release
- Initial implementation with v1 functions
- Basic `readCup` and `getDailyReading` functionality
- Used `functions.config()` for Gemini API key

