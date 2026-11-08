import { applicationDefault, initializeApp } from 'firebase-admin/app';
import { getAuth } from 'firebase-admin/auth';
import { ADMIN_EMAIL } from '../src/domain.mjs';

const [action, projectId] = process.argv.slice(2);
if (!['grant', 'revoke'].includes(action) || !projectId) {
  console.error('Usage: node scripts/admin-access.mjs <grant|revoke> <FIREBASE_PROJECT_ID>');
  process.exit(1);
}
initializeApp({ credential: applicationDefault(), projectId });
try {
  const auth = getAuth();
  const account = await auth.getUserByEmail(ADMIN_EMAIL);
  if (account.email !== ADMIN_EMAIL) throw new Error('Account email does not match the approved admin.');
  if (action === 'grant' && (!account.emailVerified || account.disabled)) {
    throw new Error('Verify the admin email and enable the account before granting access.');
  }
  const claims = { ...account.customClaims };
  if (action === 'grant') claims.admin = true;
  else delete claims.admin;
  await auth.setCustomUserClaims(account.uid, claims);
  if (action === 'revoke') await auth.revokeRefreshTokens(account.uid);
  console.log(`${action === 'grant' ? 'Granted' : 'Revoked'} admin access for ${ADMIN_EMAIL} in ${projectId}. Sign out and sign in to refresh the session.`);
} catch (error) {
  console.error(error.message);
  process.exitCode = 1;
}
