import { collection, doc, documentId, getDoc, getDocs, limit, orderBy, query, runTransaction, serverTimestamp, startAfter, where, type Firestore } from 'firebase/firestore';
import { type User } from 'firebase/auth';
import { challengeSummary, editablePatch, nextProgressTimestamp, sameRecord, isAdmin } from './domain.mjs';

export type Row = { id: string; data: Record<string, any> };
export type Kind = 'profile' | 'challenge' | 'checkIn';
export const PAGE_SIZE = 25;

export async function readPage(db: Firestore, path: string, after?: string): Promise<Row[]> {
  const constraints = [orderBy(documentId()), ...(after ? [startAfter(after)] : []), limit(PAGE_SIZE)];
  const result = await getDocs(query(collection(db, path), ...constraints));
  return result.docs.map(item => ({ id: item.id, data: item.data() }));
}

export async function readProfile(db: Firestore, uid: string): Promise<Row | null> {
  if (!uid.trim() || uid.includes('/')) throw new Error('Enter a valid user UID.');
  const result = await getDoc(doc(db, 'users', uid.trim()));
  return result.exists() ? { id: result.id, data: result.data() } : null;
}

export async function saveEdit(db: Firestore, admin: User, uid: string, kind: Kind, original: Row, values: Record<string, any>, note: string) {
  if (!isAdmin((await admin.getIdTokenResult(true)).claims)) throw new Error('Admin access is required.');
  if (!note.trim() || note.trim().length > 500) throw new Error('Add a short reason for this change (up to 500 characters).');
  const input = editablePatch(kind, values);
  const path = kind === 'profile' ? `users/${uid}` : `users/${uid}/${kind === 'challenge' ? 'challenges' : 'checkIns'}/${original.id}`;
  const reference = doc(db, path);
  const challengeId = kind === 'checkIn' ? original.data.challengeSyncId : kind === 'challenge' ? original.id : null;
  const parent = challengeId ? doc(db, `users/${uid}/challenges/${challengeId}`) : null;
  const syncState = doc(db, `users/${uid}/syncState/progress`);
  // Read before querying, then verify inside the transaction to detect added/deleted
  // check-ins as well as changes to documents already returned by the query.
  const version = challengeId ? await getDoc(syncState) : null;
  const checkSnapshots = challengeId ? await getDocs(query(collection(db, `users/${uid}/checkIns`), where('challengeSyncId', '==', challengeId))) : null;
  const audit = doc(collection(db, 'adminEdits'));

  await runTransaction(db, async transaction => {
    const currentVersion = version ? await transaction.get(syncState) : null;
    if (version && !sameRecord(version.data(), currentVersion?.data())) {
      throw new Error('Progress changed while saving. Refresh and try again.');
    }
    const fresh = await transaction.get(reference);
    if (!fresh.exists() || !sameRecord(fresh.data(), original.data)) {
      throw new Error('This record changed since you opened it. Refresh the profile and try again.');
    }
    const parentSnapshot = parent ? (kind === 'challenge' ? fresh : await transaction.get(parent)) : null;
    if (parent && !parentSnapshot?.exists()) throw new Error('The parent challenge is missing.');
    const checkIns = [];
    for (const item of checkSnapshots?.docs || []) {
      const current = await transaction.get(item.ref);
      if (!current.exists() || !sameRecord(current.data(), item.data())) throw new Error('Check-ins changed while saving. Refresh and try again.');
      checkIns.push({ ...current.data(), ...(kind === 'checkIn' && item.id === original.id ? input : {}) });
    }
    const now = Date.now();
    let patch: Record<string, any>;
    let parentPatch: Record<string, any> | null = null;
    if (kind === 'profile') {
      patch = { ...input, updatedAt: now,
        displayNameUpdatedAt: Math.max(now, Number(original.data.displayNameUpdatedAt || 0) + 1),
        challengeBehaviorUpdatedAt: Math.max(now, Number(original.data.challengeBehaviorUpdatedAt || 0) + 1) };
    } else if (kind === 'challenge') {
      patch = { ...input, ...challengeSummary({ ...fresh.data(), ...input }, checkIns), updatedAt: nextProgressTimestamp(fresh.data().updatedAt) };
    } else {
      patch = { ...input, updatedAt: nextProgressTimestamp(fresh.data().updatedAt) };
      parentPatch = { ...challengeSummary(parentSnapshot!.data(), checkIns), updatedAt: nextProgressTimestamp(parentSnapshot!.data()!.updatedAt) };
    }
    transaction.update(reference, patch);
    if (parentPatch && parent) transaction.update(parent, parentPatch);
    if (version) transaction.set(syncState, { revision: serverTimestamp() });
    transaction.set(audit, {
      actorUid: admin.uid, actorEmail: admin.email, targetPath: path,
      before: fresh.data(), after: { ...fresh.data(), ...patch },
      ...(parentPatch ? { parentPath: parent!.path, parentBefore: parentSnapshot!.data(), parentAfter: { ...parentSnapshot!.data(), ...parentPatch } } : {}),
      note: note.trim(), createdAt: serverTimestamp()
    });
  });
}
