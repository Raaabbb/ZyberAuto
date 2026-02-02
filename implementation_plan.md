# Implementation Plan

## Auth Bypass & Verification Stabilization (Feb 2, 2026)

**Goal:** Restore stable builds while isolating auth creation logic.

### Summary of Actions
- Identified duplicate `@Provides`/`@Singleton` annotations in `AppModule.kt` as the root compile error and removed duplicates.
- Reverted `UserSessionManager` and `DebugSeederViewModel` to a bypass-only baseline (no debug auth creation).
- Verified build stability after the cleanup.
- Bypassed email verification in login/registration for debug flow.
- Seeded auth accounts before dependent data, using shared password `1234qwer`.

### Current State
- Login bypass uses `UserSessionManager.setOverrideUser(uid)`.
- Build succeeds on `assembleDebug`.
- Email verification is skipped; Firestore `isVerified` is set to `true`.
- Seeded users are created in Firebase Auth and mirrored to Firestore profiles with matching UIDs.

### Next Steps
- Reintroduce `createDebugUser` with Firebase `createUserWithEmailAndPassword` using `await()` in a controlled branch.
- Add regression check: `./gradlew assembleDebug` after each change.
- If auth creation is restored, add proper error handling and rollback path.
