import { readFile } from 'node:fs/promises';
import { before, after, test } from 'node:test';
import { initializeTestEnvironment, assertSucceeds, assertFails } from '@firebase/rules-unit-testing';
import { collection, doc, getDoc, getDocs, setDoc, updateDoc, deleteDoc, serverTimestamp } from 'firebase/firestore';
import { ADMIN_EMAIL } from '../src/domain.mjs';
import assert from 'node:assert/strict';
import { readPage, readProfile, saveEdit } from '../src/data.ts';
let env;
before(async () => {
  env = await initializeTestEnvironment({ projectId: 'demo-sugarbreak-admin', firestore: { host: '127.0.0.1', port: 8088, rules: await readFile(new URL('../../firestore.rules', import.meta.url), 'utf8') } });
  await env.withSecurityRulesDisabled(async context => {
    const db = context.firestore();
    await setDoc(doc(db, 'users/user-a'), { displayName: 'Alex', challengeBehavior: 'CONTINUE', displayNameUpdatedAt: 1, challengeBehaviorUpdatedAt: 1, updatedAt: 1, onboardingCompleted: true });
    await setDoc(doc(db, 'users/user-a/challenges/challenge-a'), { targetSuccessfulDays: 30, recoveryDays: 0, status: 'ACTIVE', behavior: 'CONTINUE', updatedAt: '2026-01-01T12:00:00', syncId: 'challenge-a' });
    await setDoc(doc(db, 'users/user-a/checkIns/check-a'), { status: 'SLIP', reason: 'DESSERT', date: '2026-01-01', challengeSyncId: 'challenge-a', updatedAt: '2026-01-01T12:00:00' });
  });
});
after(async () => { await env?.cleanup(); });
const admin = () => env.authenticatedContext('admin-uid', { email: ADMIN_EMAIL, email_verified: true, admin: true }).firestore();
test('signed-out clients cannot list profiles', async () => { await assertFails(getDocs(collection(env.unauthenticatedContext().firestore(), 'users'))); });
test('regular users keep their own access but cannot list everyone', async () => {
  const db = env.authenticatedContext('user-a').firestore();
  await assertSucceeds(getDoc(doc(db, 'users/user-a')));
  await assertSucceeds(updateDoc(doc(db, 'users/user-a'), { displayName: 'Alex updated' }));
  await assertFails(getDocs(collection(db, 'users')));
  await assertFails(getDoc(doc(db, 'users/user-b')));
});
test('the approved email alone cannot access the dashboard', async () => {
  const db = env.authenticatedContext('admin-uid', { email: ADMIN_EMAIL, email_verified: true }).firestore();
  await assertFails(getDocs(collection(db, 'users')));
});
test('a custom claim for another email cannot grant access', async () => {
  const db = env.authenticatedContext('other', { email: 'other@example.com', email_verified: true, admin: true }).firestore();
  await assertFails(getDoc(doc(db, 'users/user-a')));
});
test('unverified admin cannot read other profiles', async () => {
  const db = env.authenticatedContext('admin-uid', { email: ADMIN_EMAIL, email_verified: false, admin: true }).firestore();
  await assertFails(getDoc(doc(db, 'users/user-a')));
});
test('a user cannot grant admin access through profile data', async () => {
  const db = env.authenticatedContext('user-a').firestore();
  await assertSucceeds(updateDoc(doc(db, 'users/user-a'), { admin: true }));
  await assertFails(getDocs(collection(db, 'users')));
});
test('approved admin can list and update permitted profile fields', async () => {
  const db = admin();
  await assertSucceeds(getDocs(collection(db, 'users')));
  await assertSucceeds(updateDoc(doc(db, 'users/user-a'), { displayName: 'Corrected name', displayNameUpdatedAt: 2, updatedAt: 2 }));
  await assertFails(updateDoc(doc(db, 'users/user-a'), { onboardingCompleted: false }));
});
test('admin challenge edits validate values and protect identity', async () => {
  const ref = doc(admin(), 'users/user-a/challenges/challenge-a');
  await assertSucceeds(updateDoc(ref, { targetSuccessfulDays: 45 }));
  await assertFails(updateDoc(ref, { targetSuccessfulDays: -1 }));
  await assertFails(updateDoc(ref, { syncId: 'hijacked' }));
});
test('admin check-in corrections cannot reassign the date or challenge', async () => {
  const ref = doc(admin(), 'users/user-a/checkIns/check-a');
  await assertSucceeds(updateDoc(ref, { status: 'SUCCESS', reason: null }));
  await assertFails(updateDoc(ref, { date: '2026-02-01' }));
  await assertFails(updateDoc(ref, { challengeSyncId: 'different' }));
  await assertFails(updateDoc(ref, { status: 'made-up' }));
});
test('admins cannot delete another account or its progress', async () => {
  await assertFails(deleteDoc(doc(admin(), 'users/user-a')));
  await assertFails(deleteDoc(doc(admin(), 'users/user-a/checkIns/check-a')));
});
test('audit entries are admin only and immutable', async () => {
  const ref = doc(admin(), 'adminEdits/test-edit');
  await assertSucceeds(setDoc(ref, { actorUid: 'admin-uid', actorEmail: ADMIN_EMAIL, targetPath: 'users/user-a', before: { displayName: 'Alex' }, after: { displayName: 'Corrected name' }, note: 'User requested correction', createdAt: serverTimestamp() }));
  await assertFails(updateDoc(ref, { note: 'overwrite' }));
  await assertFails(deleteDoc(ref));
  await assertFails(getDoc(doc(env.authenticatedContext('user-a').firestore(), 'adminEdits/test-edit')));
});

const adminUser = { uid: 'admin-uid', email: ADMIN_EMAIL, getIdTokenResult: async () => ({ claims: { admin: true, email_verified: true, email: ADMIN_EMAIL } }) };
test('dashboard saves profile fields and audit history atomically; rejects stale edits', async () => {
  const db = admin();
  const original = await readProfile(db, 'user-a');
  await saveEdit(db, adminUser, 'user-a', 'profile', original, { displayName: 'Reviewed profile', challengeBehavior: 'RESET_STREAK' }, 'Requested correction');
  const saved = await readProfile(db, 'user-a');
  assert.equal(saved.data.displayName, 'Reviewed profile');
  assert.equal(saved.data.challengeBehavior, 'RESET_STREAK');
  assert.ok(saved.data.displayNameUpdatedAt > original.data.displayNameUpdatedAt);
  await assert.rejects(saveEdit(db, adminUser, 'user-a', 'profile', original, { displayName: 'Stale edit', challengeBehavior: 'CONTINUE' }, 'Old form'), /record changed/);
  const audits = await readPage(db, 'adminEdits');
  assert.ok(audits.some(row => row.data.after.displayName === 'Reviewed profile'));
});

test('dashboard check-in and goal edits recalculate challenge progress and advance the sync marker', async () => {
  const db = admin();
  const original = (await readPage(db, 'users/user-a/checkIns'))[0];
  await saveEdit(db, adminUser, 'user-a', 'checkIn', original, { status: 'SLIP', reason: 'OTHER' }, 'Correct check-in');
  const saved = (await readPage(db, 'users/user-a/checkIns'))[0];
  assert.equal(saved.data.status, 'SLIP');
  assert.equal(saved.data.reason, 'OTHER');
  assert.ok(saved.data.updatedAt > original.data.updatedAt);
  const marker = await getDoc(doc(db, 'users/user-a/syncState/progress'));
  assert.ok(marker.data().revision);
  const challenge = (await readPage(db, 'users/user-a/challenges'))[0];
  await saveEdit(db, adminUser, 'user-a', 'challenge', challenge, { targetSuccessfulDays: 1, behavior: 'CONTINUE' }, 'Correct goal');
  const completed = (await readPage(db, 'users/user-a/challenges'))[0];
  assert.equal(completed.data.status, 'COMPLETED');
  assert.equal(completed.data.targetSuccessfulDays, 1);
  assert.ok((await getDoc(marker.ref)).data().revision.toMillis() >= marker.data().revision.toMillis());
});

test('invalid dashboard edits leave both progress and history untouched', async () => {
  const db = admin();
  const original = (await readPage(db, 'users/user-a/challenges'))[0];
  const auditCount = (await readPage(db, 'adminEdits')).length;
  await assert.rejects(saveEdit(db, adminUser, 'user-a', 'challenge', original, { targetSuccessfulDays: 0, behavior: 'CONTINUE' }, 'Invalid goal'), /whole number/);
  assert.deepEqual((await readPage(db, 'users/user-a/challenges'))[0], original);
  assert.equal((await readPage(db, 'adminEdits')).length, auditCount);
  await assertFails(getDoc(doc(env.authenticatedContext('user-b').firestore(), 'users/user-a/syncState/progress')));
});
