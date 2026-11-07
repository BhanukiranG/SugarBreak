export const ADMIN_EMAIL = 'bhanukiran5102@gmail.com';
export const BEHAVIORS = ['CONTINUE', 'ADD_RECOVERY_DAY', 'RESET_STREAK'];
export const STATUSES = ['SUCCESS', 'SLIP', 'SKIPPED', 'PENDING'];
export const REASONS = ['SWEET', 'DESSERT', 'SUGARY_DRINK', 'TEA_OR_COFFEE', 'OTHER'];

export function isAdmin(claims) {
  return claims?.admin === true && claims?.email_verified === true && claims?.email === ADMIN_EMAIL;
}

// Android currently stores progress timestamps as ISO LocalDateTime strings (no offset).
// Preserve that wire format and advance beyond the record we are editing.
export function nextProgressTimestamp(previous = '', now = new Date()) {
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, -1);
  if (!previous || local > previous) return local;
  const previousDate = new Date(previous.slice(0, 23) + 'Z');
  if (!Number.isFinite(previousDate.getTime())) throw new Error('This record has an invalid update time.');
  return new Date(previousDate.getTime() + 1).toISOString().slice(0, -1);
}

export function editablePatch(kind, values) {
  if (kind === 'profile') {
    const displayName = String(values.displayName ?? '').trim();
    if (!displayName || displayName.length > 100) throw new Error('Enter a name between 1 and 100 characters.');
    if (!BEHAVIORS.includes(values.challengeBehavior)) throw new Error('Choose a valid challenge rule.');
    return { displayName, challengeBehavior: values.challengeBehavior };
  }
  if (kind === 'challenge') {
    const targetSuccessfulDays = Number(values.targetSuccessfulDays);
    if (!Number.isInteger(targetSuccessfulDays) || targetSuccessfulDays < 1 || targetSuccessfulDays > 3650) {
      throw new Error('The goal must be a whole number between 1 and 3650 days.');
    }
    if (!BEHAVIORS.includes(values.behavior)) throw new Error('Choose a valid challenge rule.');
    return { targetSuccessfulDays, behavior: values.behavior };
  }
  if (kind === 'checkIn') {
    if (!STATUSES.includes(values.status)) throw new Error('Choose a valid check-in status.');
    const reason = values.status === 'SLIP' ? values.reason || null : null;
    if (reason !== null && !REASONS.includes(reason)) throw new Error('Choose a valid slip reason.');
    return { status: values.status, reason };
  }
  throw new Error('Unsupported edit.');
}

export function challengeSummary(challenge, checkIns) {
  const successes = checkIns.filter(c => c.status === 'SUCCESS').length;
  const slips = checkIns.filter(c => c.status === 'SLIP').length;
  const complete = (challenge.behavior === 'CONTINUE' ? successes + slips : successes) >= challenge.targetSuccessfulDays;
  return {
    recoveryDays: challenge.behavior === 'ADD_RECOVERY_DAY' ? slips : 0,
    status: ['PAUSED', 'CANCELLED'].includes(challenge.status) ? challenge.status : complete ? 'COMPLETED' : 'ACTIVE'
  };
}

export function sameRecord(a, b) {
  const keys = [...new Set([...Object.keys(a || {}), ...Object.keys(b || {})])].sort();
  return keys.every(key => JSON.stringify(a?.[key]) === JSON.stringify(b?.[key]));
}
