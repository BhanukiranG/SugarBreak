import React, { useEffect, useRef, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { GoogleAuthProvider, onIdTokenChanged, sendPasswordResetEmail, signInWithEmailAndPassword, signInWithPopup, signOut, type Auth, type User } from 'firebase/auth';
import type { Firestore } from 'firebase/firestore';
import { connectFirebase } from './firebase';
import { readPage, readProfile, saveEdit, PAGE_SIZE, type Kind, type Row } from './data';
import { ADMIN_EMAIL, BEHAVIORS, STATUSES, REASONS, isAdmin, editablePatch } from './domain.mjs';
import './style.css';

const words = (value: unknown) => String(value || '—').toLowerCase().replaceAll('_', ' ');
const errorMessage = (error: unknown) => error instanceof Error ? error.message : 'Something went wrong. Please try again.';
interface Source {
  profiles: (after?: string) => Promise<Row[]>;
  profile: (uid: string) => Promise<Row | null>;
  records: (uid: string, kind: Kind, after?: string) => Promise<Row[]>;
  save: (uid: string, kind: Kind, original: Row, values: Record<string, any>, note: string) => Promise<void>;
}
function Brand() { return <div className="brand"><img src="/logo.jpg" alt=""/><span>SugarBreak<small>ADMIN WORKSPACE</small></span></div>; }
function Badge({ children, tone = '' }: { children: React.ReactNode; tone?: string }) { return <span className={`badge ${tone}`}>{children}</span>; }
function Notice({ children }: { children: React.ReactNode }) { return <div className="notice" role="alert">{children}</div>; }

function App() {
  const [connection, setConnection] = useState<{ auth: Auth; db: Firestore } | null>(null);
  const [user, setUser] = useState<User | null>(null);
  const [allowed, setAllowed] = useState(false);
  const [ready, setReady] = useState(false);
  const [problem, setProblem] = useState('');
  useEffect(() => {
    let dispose: (() => void) | undefined, mounted = true, revision = 0;
    let activeUid: string | null = null;
    connectFirebase().then(value => {
      if (!mounted) return;
      setConnection(value);
      dispose = onIdTokenChanged(value.auth, async account => {
        const current = ++revision;
        // Refreshing the same account's token must not dismiss an in-flight editor.
        if (!account || account.uid !== activeUid) { setAllowed(false); setReady(false); }
        activeUid = account?.uid ?? null;
        setUser(account); setProblem('');
        try {
          const claims = account ? (await account.getIdTokenResult()).claims : null;
          if (!mounted || current !== revision) return;
          setAllowed(isAdmin(claims));
          if (account && !isAdmin(claims)) setProblem('This account does not have admin access. Only the approved, verified admin account can open this workspace.');
        } catch (error) { if (mounted && current === revision) { setAllowed(false); setProblem(errorMessage(error)); } }
        finally { if (mounted && current === revision) setReady(true); }
      });
    }).catch(error => { if (mounted) { setProblem(errorMessage(error)); setReady(true); } });
    return () => { mounted = false; revision++; dispose?.(); };
  }, []);
  if (!ready) return <div className="center"><Brand/><p role="status">Opening your workspace…</p></div>;
  if (allowed && user && connection) {
    const source: Source = {
      profiles: after => readPage(connection.db, 'users', after),
      profile: uid => readProfile(connection.db, uid),
      records: (uid, kind, after) => readPage(connection.db, `users/${uid}/${kind === 'challenge' ? 'challenges' : 'checkIns'}`, after),
      save: (uid, kind, original, values, note) => saveEdit(connection.db, user, uid, kind, original, values, note)
    };
    return <Dashboard key={user.uid} source={source} onSignOut={() => signOut(connection.auth)} preview={false}/>;
  }
  return <Login connection={connection} user={user} problem={problem}/>;
}

function Login({ connection, user, problem }: { connection: { auth: Auth; db: Firestore } | null; user: User | null; problem: string }) {
  const [password, setPassword] = useState(''), [busy, setBusy] = useState(false), [error, setError] = useState(''), [message, setMessage] = useState('');
  const perform = async (action: () => Promise<unknown>) => {
    if (busy) return; setBusy(true); setError(''); setMessage('');
    try { await action(); } catch (err) { setError(errorMessage(err)); } finally { setBusy(false); }
  };
  return <div className="login-layout">
    <section className="login-story"><Brand/><div><span className="eyebrow">A LITTLE LESS SUGAR. A LITTLE MORE CARE.</span><h1>Support every<br/>small win.</h1><p>A private workspace to understand progress and keep your community’s records on track.</p><div className="story-mark" aria-hidden="true">↗</div></div><span className="story-footer">SugarBreak · Administration</span></section>
    <main className="login-form"><div className="login-card"><Badge>Restricted access</Badge><h2>Welcome back.</h2><p className="muted">Sign in with your approved admin account.</p>
      {(problem || error) && <Notice>{error || problem}</Notice>}
      {message && <p role="status" className="success">{message}</p>}
      {user ? <button onClick={() => perform(() => signOut(connection!.auth))} disabled={busy}>Sign out and use another account</button> : <>
        <form onSubmit={event => { event.preventDefault(); if (connection) perform(() => signInWithEmailAndPassword(connection.auth, ADMIN_EMAIL, password)); }}>
          <label>Email<input type="email" value={ADMIN_EMAIL} readOnly autoComplete="username"/></label>
          <label>Password<input type="password" required value={password} onChange={event => setPassword(event.target.value)} autoComplete="current-password" disabled={busy}/></label>
          <button disabled={!connection || busy} className="primary wide">{busy ? 'Signing in…' : 'Sign in to workspace →'}</button>
        </form><div className="divider">or</div>
        <button className="wide secondary" disabled={!connection || busy} onClick={() => perform(() => signInWithPopup(connection!.auth, new GoogleAuthProvider()))}>Continue with Google</button>
        <button className="link" disabled={!connection || busy} onClick={() => perform(async () => { await sendPasswordResetEmail(connection!.auth, ADMIN_EMAIL); setMessage('Password reset email sent. Check your inbox.'); })}>Forgot password?</button>
      </>}
      <p className="footnote">Access is checked by Firebase. Regular app accounts cannot view this dashboard.</p>
    </div></main>
  </div>;
}

function Dashboard({ source, onSignOut, preview }: { source: Source; onSignOut: () => Promise<void>; preview: boolean }) {
  const [profiles, setProfiles] = useState<Row[]>([]), [cursorStack, setCursorStack] = useState<(string | undefined)[]>([undefined]);
  const [selected, setSelected] = useState<Row | null>(null), [tab, setTab] = useState<Kind>('profile'), [records, setRecords] = useState<Row[]>([]);
  const [recordStack, setRecordStack] = useState<(string | undefined)[]>([undefined]);
  const [busy, setBusy] = useState(false), [loadingRows, setLoadingRows] = useState(false), [error, setError] = useState(''), [message, setMessage] = useState('');
  const [filter, setFilter] = useState(''), [uid, setUid] = useState(''), [edit, setEdit] = useState<{ kind: Kind; record: Row } | null>(null);
  const [signingOut, setSigningOut] = useState(false);
  const profileRequest = useRef(0), recordsRequest = useRef(0);
  const sourceRef = useRef(source); sourceRef.current = source;
  useEffect(() => {
    const request = ++profileRequest.current; setBusy(true); setError('');
    sourceRef.current.profiles(cursorStack.at(-1)).then(result => { if (request === profileRequest.current) setProfiles(result); }).catch(err => { if (request === profileRequest.current) setError(errorMessage(err)); }).finally(() => { if (request === profileRequest.current) setBusy(false); });
    return () => { profileRequest.current++; };
  }, [cursorStack]);
  useEffect(() => {
    const request = ++recordsRequest.current; setRecords([]); setError('');
    if (!selected || tab === 'profile') { setLoadingRows(false); return; }
    setLoadingRows(true);
    sourceRef.current.records(selected.id, tab, recordStack.at(-1)).then(result => { if (request === recordsRequest.current) setRecords(result); }).catch(err => { if (request === recordsRequest.current) setError(errorMessage(err)); }).finally(() => { if (request === recordsRequest.current) setLoadingRows(false); });
    return () => { recordsRequest.current++; };
  }, [selected?.id, tab, recordStack]);
  const select = (profile: Row) => { setSelected(profile); setTab('profile'); setRecordStack([undefined]); setMessage(''); };
  const refresh = async () => {
    setError(''); setBusy(true);
    try {
      if (selected) { const fresh = await source.profile(selected.id); setSelected(fresh); }
      setCursorStack(stack => [...stack]); setRecordStack(stack => [...stack]);
    } catch (err) { setError(errorMessage(err)); } finally { setBusy(false); }
  };
  const filtered = profiles.filter(row => `${row.data.displayName || ''} ${row.id}`.toLowerCase().includes(filter.toLowerCase()));
  return <div className="workspace">
    <aside className="rail"><Brand/><div className="rail-label">WORKSPACE</div><div className="nav-active"><span>▦</span> User profiles <span className="nav-dot"/></div><div className="rail-note"><span className="shield">✓</span><strong>Private by design</strong><p>Approved admin access.<br/>Every saved edit is recorded.</p></div><div className="admin-account"><div className="avatar">BK</div><div><strong>Bhanu Kiran</strong><small>Administrator</small></div></div></aside>
    <main className="main"><header className="topbar"><span>SugarBreak <span className="slash">/</span> Administration</span><div>{preview && <Badge tone="amber">Preview · sample data</Badge>}<Badge>Admin access</Badge><button className="quiet" disabled={signingOut} onClick={async () => { setSigningOut(true); try { await onSignOut(); } catch (err) { setError(errorMessage(err)); } finally { setSigningOut(false); } }}>{signingOut ? 'Signing out…' : 'Sign out'}</button></div></header>
      <div className="page-heading"><div><div className="eyebrow">COMMUNITY CARE</div><h1>User profiles</h1><p className="muted">See their progress. Help keep every record accurate.</p></div><button className="secondary" onClick={refresh} disabled={busy}>↻ Refresh data</button></div>
      <div className="summary-strip"><div><span className="summary-icon">◎</span><div><small>Profiles on this page</small><strong>{busy ? '—' : profiles.length}</strong></div></div><div><span className="summary-icon mint">✓</span><div><small>Access level</small><strong>View & edit</strong></div></div><div className="summary-info"><span>↗</span><p>Changes appear in the Android app on its next sync.</p></div></div>
      {error && <Notice>{error} <button className="link" onClick={refresh}>Retry</button></Notice>}{message && <p className="success" role="status">{message}</p>}
      <div className="data-layout"><section className="profile-list panel"><div className="panel-heading"><h2>Synced profiles</h2><Badge tone="neutral">{profiles.length}</Badge></div><div className="search-area"><label className="sr-only" htmlFor="filter">Filter this page</label><input id="filter" placeholder="Filter this page by name or UID" value={filter} onChange={e => setFilter(e.target.value)}/><form className="uid-search" onSubmit={async e => { e.preventDefault(); setError(''); setBusy(true); try { const found = await source.profile(uid); if (found) select(found); else setError('No synced profile found for that UID.'); } catch (err) { setError(errorMessage(err)); } finally { setBusy(false); } }}><label className="sr-only" htmlFor="uid">Find exact UID</label><input id="uid" placeholder="Find exact UID" value={uid} onChange={e => setUid(e.target.value)} required/><button className="quiet" disabled={busy}>Find</button></form></div>
      <div className="profile-rows" aria-busy={busy}>{busy ? <p className="empty" role="status">Loading profiles…</p> : filtered.length ? filtered.map(row => <button key={row.id} className={`profile-row ${selected?.id === row.id ? 'selected' : ''}`} onClick={() => select(row)}><span className="avatar">{String(row.data.displayName || '?').slice(0, 2).toUpperCase()}</span><span className="profile-name"><strong>{row.data.displayName || 'Unnamed profile'}</strong><small title={row.id}>{row.id}</small></span><span className="chevron">›</span></button>) : <div className="empty">No profiles here.<small>Only accounts with synced app data appear.</small></div>}</div>
      <div className="pager"><button className="quiet" disabled={busy || cursorStack.length === 1} onClick={() => setCursorStack(stack => stack.slice(0, -1))}>← Previous</button><span>Page {cursorStack.length}</span><button className="quiet" disabled={busy || profiles.length < PAGE_SIZE} onClick={() => setCursorStack(stack => [...stack, profiles.at(-1)!.id])}>Next →</button></div></section>
      <section className="detail panel">{!selected ? <div className="empty detail-empty"><span className="empty-symbol">◎</span><h2>A closer look at their journey</h2><p>Select a profile to view progress and manage records.</p></div> : <><div className="detail-heading"><div className="avatar large">{String(selected.data.displayName || '?').slice(0, 2).toUpperCase()}</div><div><h2>{selected.data.displayName || 'Unnamed profile'}</h2><code>{selected.id}</code></div><Badge tone={selected.data.onboardingCompleted ? '' : 'amber'}>{selected.data.onboardingCompleted ? 'Onboarded' : 'Onboarding'}</Badge></div><nav className="tabs" aria-label="Profile sections">{(['profile', 'challenge', 'checkIn'] as Kind[]).map(kind => <button key={kind} aria-current={tab === kind ? 'page' : undefined} className={tab === kind ? 'active' : ''} onClick={() => { setTab(kind); setRecordStack([undefined]); }}>{kind === 'profile' ? 'Overview' : kind === 'challenge' ? 'Challenges' : 'Check-ins'}</button>)}</nav>
        {tab === 'profile' ? <div className="overview"><div className="section-title"><h3>Profile details</h3><button className="secondary" onClick={() => setEdit({ kind: 'profile', record: selected })}>Edit profile</button></div><dl><div><dt>Display name</dt><dd>{selected.data.displayName || '—'}</dd></div><div><dt>Default challenge rule</dt><dd>{words(selected.data.challengeBehavior || 'CONTINUE')}</dd></div><div><dt>Onboarding</dt><dd>{selected.data.onboardingCompleted ? 'Complete' : 'Not completed'}</dd></div><div><dt>Last profile sync</dt><dd>{selected.data.updatedAt ? new Date(selected.data.updatedAt).toLocaleString() : 'Not recorded'}</dd></div></dl><div className="detail-note">Profile settings and progress belong to this account. Theme and notification preferences stay on the user’s device.</div></div> : <div className="record-area"><div className="section-title"><h3>{tab === 'challenge' ? 'Challenge records' : 'Daily check-ins'}</h3><span className="muted">{records.length} on this page</span></div>{loadingRows ? <p className="empty" role="status">Loading records…</p> : !records.length ? <p className="empty">No {tab === 'challenge' ? 'challenges' : 'check-ins'} on this page.</p> : <div className="table-scroll"><table><thead><tr><th>{tab === 'challenge' ? 'Started' : 'Date'}</th><th>Status</th><th>{tab === 'challenge' ? 'Goal' : 'Reason'}</th><th><span className="sr-only">Actions</span></th></tr></thead><tbody>{records.map(row => <tr key={row.id}><td><strong>{row.data[tab === 'challenge' ? 'startDate' : 'date'] || '—'}</strong><small className="record-id" title={row.id}>{row.id}</small></td><td><Badge tone={['SLIP', 'CANCELLED', 'PAUSED'].includes(row.data.status) ? 'amber' : ''}>{words(row.data.status)}</Badge></td><td>{tab === 'challenge' ? `${row.data.targetSuccessfulDays} days` : words(row.data.reason)}</td><td><button className="link" onClick={() => setEdit({ kind: tab, record: row })} aria-label={`Edit ${tab} ${row.data.date || row.data.startDate}`}>Edit ↗</button></td></tr>)}</tbody></table></div>}<div className="pager"><button className="quiet" disabled={loadingRows || recordStack.length === 1} onClick={() => setRecordStack(stack => stack.slice(0, -1))}>← Previous</button><span>Page {recordStack.length}</span><button className="quiet" disabled={loadingRows || records.length < PAGE_SIZE} onClick={() => setRecordStack(stack => [...stack, records.at(-1)!.id])}>Next →</button></div></div>}
      </>}</section></div><footer>Private workspace <span>•</span> Signed in as {ADMIN_EMAIL}</footer>
    </main>{edit && selected && <Editor kind={edit.kind} original={edit.record} onClose={() => setEdit(null)} onSave={async (values, note) => { await source.save(selected.id, edit.kind, edit.record, values, note); setEdit(null); setMessage(preview ? 'Preview updated. No real data was changed.' : 'Changes saved. The edit was recorded in the admin history.'); await refresh(); }}/>}</div>;
}

function Editor({ kind, original, onClose, onSave }: { kind: Kind; original: Row; onClose: () => void; onSave: (values: Record<string, any>, note: string) => Promise<void> }) {
  const dialog = useRef<HTMLDialogElement>(null);
  const [values, setValues] = useState<Record<string, any>>({ ...original.data, challengeBehavior: original.data.challengeBehavior || 'CONTINUE' });
  const [note, setNote] = useState(''), [busy, setBusy] = useState(false), [error, setError] = useState('');
  useEffect(() => { dialog.current?.showModal(); }, []);
  const change = (key: string, value: string) => setValues(old => ({ ...old, [key]: value }));
  const rules = (key: string) => <label>Challenge rule<select value={values[key]} onChange={e => change(key, e.target.value)}>{BEHAVIORS.map(value => <option key={value} value={value}>{words(value)}</option>)}</select></label>;
  return <dialog ref={dialog} onCancel={e => { e.preventDefault(); if (!busy) onClose(); }} aria-labelledby="edit-title"><form onSubmit={async e => { e.preventDefault(); if (busy) return; setBusy(true); setError(''); try { editablePatch(kind, values); await onSave(values, note); } catch (err) { setError(errorMessage(err)); } finally { setBusy(false); } }}><div className="modal-heading"><div><span className="eyebrow">ADMIN EDIT</span><h2 id="edit-title">Edit {kind === 'checkIn' ? 'check-in' : kind}</h2></div><button type="button" className="quiet" aria-label="Close editor" onClick={onClose} disabled={busy}>✕</button></div><p className="muted">Review your changes before saving. A history entry will record this edit.</p><fieldset disabled={busy}>
    {kind === 'profile' && <><label>Display name<input required maxLength={100} value={values.displayName || ''} onChange={e => change('displayName', e.target.value)}/></label>{rules('challengeBehavior')}</>}
    {kind === 'challenge' && <><label>Goal (days)<input type="number" required min={1} max={3650} step={1} value={values.targetSuccessfulDays} onChange={e => change('targetSuccessfulDays', e.target.value)}/></label>{rules('behavior')}<p className="footnote">Completion and recovery days are recalculated from this challenge’s check-ins. Paused or cancelled challenges keep that state.</p></>}
    {kind === 'checkIn' && <><label>Status<select value={values.status} onChange={e => change('status', e.target.value)}>{STATUSES.map(value => <option key={value} value={value}>{words(value)}</option>)}</select></label>{values.status === 'SLIP' && <label>Reason<select value={values.reason || ''} onChange={e => change('reason', e.target.value)}><option value="">Not specified</option>{REASONS.map(value => <option key={value} value={value}>{words(value)}</option>)}</select></label>}<p className="footnote">The date and linked challenge stay fixed. Challenge progress is recalculated with this correction.</p></>}
    <label>Reason for this edit<textarea required maxLength={500} rows={3} value={note} onChange={e => setNote(e.target.value)} placeholder="For example: corrected a check-in reported by the user"/></label></fieldset>{error && <Notice>{error}</Notice>}<div className="modal-actions"><button type="button" className="secondary" onClick={onClose} disabled={busy}>Cancel</button><button className="primary" disabled={busy}>{busy ? 'Saving…' : 'Save changes'}</button></div></form></dialog>;
}

async function mount() {
  const root = createRoot(document.getElementById('root')!);
  if (import.meta.env.DEV && new URLSearchParams(location.search).has('preview')) {
    const { previewSource } = await import('./preview');
    root.render(<Dashboard source={previewSource} preview onSignOut={async () => { location.href = '/'; }}/>);
  } else root.render(<App/>);
}
void mount();
