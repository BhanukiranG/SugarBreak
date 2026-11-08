import { editablePatch } from './domain.mjs';
import type { Row, Kind } from './data';
const profiles: Row[] = ['Ananya Rao', 'Rohan Mehta', 'Priya Sharma', 'Arjun Patel', 'Sneha Reddy', 'Vikram Singh'].map((name, index) => ({ id: `sample-user-${index + 1}`, data: { displayName: name, onboardingCompleted: index !== 4, challengeBehavior: index % 2 ? 'ADD_RECOVERY_DAY' : 'CONTINUE', updatedAt: Date.now() - index * 86400000 } }));
const challenges: Row[] = [{ id: 'sample-challenge', data: { startDate: '2026-11-01', targetSuccessfulDays: 30, status: 'ACTIVE', behavior: 'CONTINUE', recoveryDays: 0, updatedAt: '2026-11-07T09:00:00' } }];
const checks: Row[] = Array.from({ length: 7 }, (_, index) => ({ id: `sample-check-${index}`, data: { date: `2026-11-0${index + 1}`, status: index === 3 ? 'SLIP' : 'SUCCESS', reason: index === 3 ? 'DESSERT' : null, challengeSyncId: 'sample-challenge' } }));
export const previewSource = {
  profiles: async () => profiles,
  profile: async (uid: string) => profiles.find(profile => profile.id === uid) || null,
  records: async (_uid: string, kind: Kind) => kind === 'challenge' ? challenges : checks,
  save: async (_uid: string, kind: Kind, original: Row, values: Record<string, any>, _note: string) => { Object.assign(original.data, editablePatch(kind, values)); }
};
