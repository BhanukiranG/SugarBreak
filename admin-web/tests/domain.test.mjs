import test from 'node:test';
import assert from 'node:assert/strict';
import { ADMIN_EMAIL, isAdmin, editablePatch, challengeSummary, nextProgressTimestamp, sameRecord } from '../src/domain.mjs';

test('access requires the exact approved verified account and a trusted claim', () => {
  assert.equal(isAdmin({ email: ADMIN_EMAIL, email_verified: true, admin: true }), true);
  for (const claims of [null, { email: ADMIN_EMAIL, admin: true }, { email: ADMIN_EMAIL, email_verified: true }, { email: 'someone@example.com', email_verified: true, admin: true }]) assert.equal(isAdmin(claims), false);
});
test('only editable profile fields are returned', () => {
  assert.deepEqual(editablePatch('profile', { displayName: ' Alex ', challengeBehavior: 'CONTINUE', admin: true, onboardingCompleted: false }), { displayName: 'Alex', challengeBehavior: 'CONTINUE' });
  assert.throws(() => editablePatch('profile', { displayName: '', challengeBehavior: 'CONTINUE' }));
});
test('goals must be bounded whole numbers', () => {
  for (const value of [0, -1, 1.5, 3651, 'bad']) assert.throws(() => editablePatch('challenge', { targetSuccessfulDays: value, behavior: 'CONTINUE' }));
  assert.equal(editablePatch('challenge', { targetSuccessfulDays: '30', behavior: 'RESET_STREAK' }).targetSuccessfulDays, 30);
});
test('correcting a slip clears the old reason', () => {
  assert.deepEqual(editablePatch('checkIn', { status: 'SUCCESS', reason: 'DESSERT' }), { status: 'SUCCESS', reason: null });
  assert.throws(() => editablePatch('checkIn', { status: 'SLIP', reason: 'invalid' }));
});
test('correcting check-ins recomputes completion and recovery days', () => {
  const challenge = { status: 'ACTIVE', targetSuccessfulDays: 2, behavior: 'ADD_RECOVERY_DAY' };
  assert.deepEqual(challengeSummary(challenge, [{ status: 'SUCCESS' }, { status: 'SLIP' }]), { recoveryDays: 1, status: 'ACTIVE' });
  assert.deepEqual(challengeSummary(challenge, [{ status: 'SUCCESS' }, { status: 'SUCCESS' }]), { recoveryDays: 0, status: 'COMPLETED' });
  assert.equal(challengeSummary({ ...challenge, behavior: 'CONTINUE' }, [{ status: 'SUCCESS' }, { status: 'SLIP' }]).status, 'COMPLETED');
  assert.equal(challengeSummary({ ...challenge, status: 'CANCELLED' }, []).status, 'CANCELLED');
});
test('progress timestamps remain compatible with Android including nanosecond values', () => {
  const previous = '2099-01-01T10:00:00.999999999';
  const next = nextProgressTimestamp(previous, new Date('2026-01-01T00:00:00Z'));
  assert.equal(next, '2099-01-01T10:00:01.000');
  assert.ok(next > previous);
  assert.equal(next.endsWith('Z'), false);
});
test('concurrent field changes are detected even when timestamps tie', () => {
  assert.equal(sameRecord({ status: 'SUCCESS', updatedAt: 'a' }, { status: 'SLIP', updatedAt: 'a' }), false);
  assert.equal(sameRecord({ status: 'SUCCESS', updatedAt: 'a' }, { updatedAt: 'a', status: 'SUCCESS' }), true);
});
