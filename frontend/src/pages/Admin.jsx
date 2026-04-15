import { useEffect, useState } from 'react';
import {
  adminListSubs, adminCancelSub, adminDeleteSub,
  adminListUsers, adminDeleteUser, adminListPlans,
} from '../api.js';
import ErrorModal from '../components/ErrorModal.jsx';

export default function Admin() {
  const [subs, setSubs]     = useState([]);
  const [users, setUsers]   = useState([]);
  const [plans, setPlans]   = useState([]);
  const [error, setError]   = useState(null);
  const [section, setSection] = useState('subs');

  const load = async () => {
    const s = await adminListSubs();
    if (s.error) setError(s.error); else setSubs(Array.isArray(s) ? s : []);
    const u = await adminListUsers();
    if (Array.isArray(u)) setUsers(u);
    const p = await adminListPlans();
    if (Array.isArray(p)) setPlans(p);
  };
  useEffect(() => { load(); }, []);

  const onCancelSub = async (subId) => {
    if (!window.confirm('Cancel this subscription?')) return;
    const res = await adminCancelSub(subId);
    if (res.error) setError(res.error);
    load();
  };
  const onDeleteSub = async (subId) => {
    if (!window.confirm('Permanently delete this subscription? This cannot be undone.')) return;
    const res = await adminDeleteSub(subId);
    if (res.error) setError(res.error);
    load();
  };
  const onDeleteUser = async (userId, name) => {
    if (!window.confirm(`Delete user "${name}" and all their subscriptions?`)) return;
    const res = await adminDeleteUser(userId);
    if (res.error) setError(res.error);
    load();
  };

  return (
    <div className="page">
      <h2>Admin Dashboard</h2>
      <nav className="tabs" style={{ marginBottom: 16 }}>
        <button className={section === 'subs'  ? 'tab on' : 'tab'} onClick={() => setSection('subs')}>Subscriptions ({subs.length})</button>
        <button className={section === 'users' ? 'tab on' : 'tab'} onClick={() => setSection('users')}>Users ({users.length})</button>
        <button className={section === 'plans' ? 'tab on' : 'tab'} onClick={() => setSection('plans')}>Plans ({plans.length})</button>
      </nav>

      {section === 'subs' && (
        <>
          <h3>All Subscriptions</h3>
          <table>
            <thead><tr><th>Member</th><th>Email</th><th>Plan</th><th>Cycle</th><th>Status</th><th>Started</th><th>Expires</th><th>Actions</th></tr></thead>
            <tbody>
              {subs.length === 0 && <tr><td colSpan="8" className="muted">No subscriptions.</td></tr>}
              {subs.map((s) => (
                <tr key={s.subId}>
                  <td>{s.userFullName || '—'}</td>
                  <td>{s.userEmail || '—'}</td>
                  <td>{s.planName || s.planCode}</td>
                  <td>{s.billingCycle}</td>
                  <td><span className={'badge ' + s.status.toLowerCase()}>{s.status}</span></td>
                  <td>{s.startedAt?.slice(0, 10)}</td>
                  <td>{s.expiresAt?.slice(0, 10)}</td>
                  <td>
                    <button disabled={s.status === 'CANCELLED'} onClick={() => onCancelSub(s.subId)}>Cancel</button>
                    <button onClick={() => onDeleteSub(s.subId)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}

      {section === 'users' && (
        <>
          <h3>All Users</h3>
          <table>
            <thead><tr><th>Name</th><th>Email</th><th>Age</th><th>Active Plan</th><th>Sub Status</th><th>Joined</th><th>Actions</th></tr></thead>
            <tbody>
              {users.length === 0 && <tr><td colSpan="7" className="muted">No users yet.</td></tr>}
              {users.map((u) => (
                <tr key={u.userId}>
                  <td>{u.fullName}</td>
                  <td>{u.email}</td>
                  <td>{u.age || '—'}</td>
                  <td>{u.activeSubPlan || '—'}</td>
                  <td>{u.activeSubStatus ? <span className={'badge ' + u.activeSubStatus.toLowerCase()}>{u.activeSubStatus}</span> : <span className="muted">none</span>}</td>
                  <td>{u.createdAt?.slice(0, 10)}</td>
                  <td><button onClick={() => onDeleteUser(u.userId, u.fullName)}>Delete</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}

      {section === 'plans' && (
        <>
          <h3>Plans</h3>
          <table>
            <thead><tr><th>Code</th><th>Name</th><th>Monthly</th><th>Yearly</th><th>Screens</th><th>Quality</th><th>Trial Days</th></tr></thead>
            <tbody>
              {plans.map((p) => (
                <tr key={p.code}>
                  <td>{p.code}</td>
                  <td>{p.name}</td>
                  <td>₹{p.monthlyInr}</td>
                  <td>₹{p.yearlyInr}</td>
                  <td>{p.screens}</td>
                  <td>{p.quality}</td>
                  <td>{p.trialDays}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}

      <ErrorModal message={error} onClose={() => setError(null)} />
    </div>
  );
}
