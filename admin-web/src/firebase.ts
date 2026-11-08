import { initializeApp, type FirebaseOptions } from 'firebase/app';
import { getAuth, browserSessionPersistence, setPersistence } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';

export async function connectFirebase() {
  let config: FirebaseOptions;
  if (import.meta.env.VITE_FIREBASE_CONFIG) {
    config = JSON.parse(import.meta.env.VITE_FIREBASE_CONFIG);
  } else {
    const response = await fetch('/__/firebase/init.json');
    if (!response.ok) throw new Error('Firebase web configuration is required. Follow admin-web/README.md to connect this dashboard.');
    try { config = await response.json(); }
    catch { throw new Error('Firebase web configuration is required. Follow admin-web/README.md to connect this dashboard.'); }
  }
  if (!config.apiKey || !config.projectId || !config.appId) throw new Error('Firebase web configuration is incomplete.');
  const app = initializeApp(config);
  const auth = getAuth(app);
  await setPersistence(auth, browserSessionPersistence);
  return { auth, db: getFirestore(app) };
}
