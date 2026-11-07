# SugarBreak admin workspace

A static React website for viewing and correcting the existing Android app's Firestore data. Only **bhanukiran5102@gmail.com**, with a verified email and the Firebase Auth custom claim `admin: true`, can access other accounts. Both the website and Firestore rules enforce this requirement.

The dashboard lists synced profiles (25 per page), supports exact UID lookup, and edits display names, saved challenge rules, challenge goals/rules, and check-in status/reason. Each dashboard save writes an immutable `adminEdits` history entry in the same transaction. There is no user deletion, registration, credential management, or Firebase Auth user directory. Accounts without a synced `users/{uid}` document do not appear.

## 1. Register a Firebase web app

Use the **same Firebase project** as `app/google-services.json`; do not create another database.

1. Open Firebase Console → Project settings → General → Your apps → **Add app → Web**.
2. Name it `SugarBreak Admin` and register it. Copy its public Firebase configuration.
3. In this directory, copy `.env.example` to `.env.local`. Replace the example JSON with the web configuration on a single line:

   ```dotenv
   VITE_FIREBASE_CONFIG={"apiKey":"...","authDomain":"YOUR_PROJECT.firebaseapp.com","projectId":"YOUR_PROJECT","appId":"YOUR_WEB_APP_ID"}
   ```

4. Keep the existing Google and Email/Password sign-in providers enabled. Under Authentication → Settings → Authorized domains, add `localhost` for development and the final Hosting domain if absent.

Use the web app's ID, not the Android app ID. The web configuration is public client configuration; never put an Admin SDK service-account key or private credential in a `VITE_` variable. `.env.local` is ignored by Git. On Firebase Hosting the app can also read `/__/firebase/init.json` when no build-time configuration is supplied.

## 2. Grant the approved account access

The email must already exist in Firebase Authentication and be verified. Signing in with that email alone does **not** grant admin permission.

Install Node.js 22.18+ (Node 24 recommended), pnpm, and dependencies:

```powershell
cd C:\Users\bhanu\AndroidStudioProjects\NoSugar\admin-web
pnpm install --frozen-lockfile
```

Run the grant script from a trusted computer using Application Default Credentials with permission to manage Firebase Authentication in this project. For example, use a project service-account credential saved **outside this repository**, then:

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS = 'C:\private\sugarbreak-service-account.json'
pnpm admin:grant YOUR_FIREBASE_PROJECT_ID
Remove-Item Env:GOOGLE_APPLICATION_CREDENTIALS
```

The script only grants the approved email and preserves any other claims. Sign out and back in after granting it. Never paste private keys into chat, commit them, or include them in the website build. Firebase CLI login and Admin SDK credentials are separate; `firebase login` alone does not authenticate this script.

To revoke access, supply the same trusted credentials and run `pnpm admin:revoke YOUR_FIREBASE_PROJECT_ID`. Existing ID tokens can remain valid until they expire (typically up to one hour); revocation prevents refresh. For immediate enforcement, also remove the approved email from `isAdmin()` in the Firestore rules and deploy those rules.

## 3. Run and verify

```powershell
pnpm dev
pnpm test
pnpm test:rules
pnpm build
```

The rules/integration tests need Java 21+ and start a Firestore emulator on port 8088 under `demo-sugarbreak-admin`. They never connect to production. On this Windows machine, Java is available at `C:\Program Files\Android\Android Studio\jbr`; set `JAVA_HOME` and add its `bin` directory to `PATH` if necessary.

Open `http://localhost:5173/` for real authentication, or `http://localhost:5173/?preview` for a clearly labelled, in-memory sample dashboard. Sample data and preview access are excluded from production builds.

## 4. Publish to Firebase Hosting

This is static Firebase Hosting, not Firebase App Hosting or Cloud Functions. It can run on the Spark plan within the project's free Hosting and Firestore quotas, which are shared with the Android app. Check the [current Firebase pricing and limits](https://firebase.google.com/pricing) before deployment.

From `admin-web`, log in to the project owner account, create a separate Hosting site with a globally unique site ID, and map it to the `admin` target. Replace the uppercase placeholders:

```powershell
pnpm exec firebase login
pnpm exec firebase hosting:sites:create YOUR_UNIQUE_ADMIN_SITE_ID --project YOUR_FIREBASE_PROJECT_ID --config ../firebase.json
pnpm exec firebase target:apply hosting admin YOUR_UNIQUE_ADMIN_SITE_ID --project YOUR_FIREBASE_PROJECT_ID --config ../firebase.json
pnpm build
pnpm exec firebase deploy --only firestore:rules,hosting:admin --project YOUR_FIREBASE_PROJECT_ID --config ../firebase.json
```

The target mapping is saved to the root `.firebaserc`. If a dedicated admin site already exists, skip site creation and map that site. Avoid mapping to an unrelated existing website. The resulting URL is `https://YOUR_UNIQUE_ADMIN_SITE_ID.web.app`.

The login page is publicly reachable; progress data is protected by Firebase rules. Test with the approved admin and a regular account after deployment. Do not weaken the rules to troubleshoot sign-in; check verified email, claim, project configuration, and authorized domains.

## Sync and editing behavior

- Saves require connectivity, use Firestore transactions, reject stale forms, and write audit history atomically. Invalid or failed saves leave the editor open for retry. Audit entries can be inspected in the Firebase Console; this version has no history screen. Rules prevent modifying/deleting history, but do not require a trusted admin using another client to create an audit entry for every write.
- Deploy the Android changes in this branch along with the dashboard. Updated Android sync rechecks cloud versions before writing and advances `users/{uid}/syncState/progress` on uploads/deletions. The dashboard uses this marker to detect changes to the set of check-ins during a save. Old app versions do not participate in this coordination and can still overwrite concurrent edits; update supported clients before using admin corrections.
- Android restores edits on its next sync. Local Room writes remain available for retry when offline. Profile values use millisecond timestamps; progress records retain the app's existing local-time ISO format. Across devices with different time zones or incorrect clocks, last-write ordering retains that existing limitation. A future migration should use server-based versioning throughout.
- Check-in corrections and goal/rule changes recalculate completion and recovery days. Paused/cancelled challenges retain their state. Dates, record identity, ownership, and onboarding are read-only in the dashboard. Theme and reminders remain device-local.
- Browser authentication uses session persistence; Firestore uses memory caching. Signing out removes the dashboard from view. No service-account credential is used by the browser.

Official references: [custom claims](https://firebase.google.com/docs/auth/admin/custom-claims), [Firestore transactions](https://firebase.google.com/docs/firestore/manage-data/transactions), [Firebase Hosting](https://firebase.google.com/docs/hosting).
