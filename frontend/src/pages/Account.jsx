import { useEffect, useState } from 'react';
import { mySubs, cancelSub, renewSub, changeSub, myInvoices, myPayments, getPlans } from '../api.js';
import ErrorModal from '../components/ErrorModal.jsx';
import ConfirmModal from '../components/ConfirmModal.jsx';

export default function Account({ user }) {
  const [subs, setSubs]     = useState([]);
  const [invoices, setInv]  = useState([]);
  const [payments, setPays] = useState([]);
  const [plansMap, setPM]   = useState({});
  const [error, setError]   = useState(null);
  const [confirm, setConfirm] = useState(null);
  const [tab, setTab] = useState('subs');

  const load = async () => {
    const s = await mySubs(user.userId);
    if (s.error) setError(s.error); else setSubs(Array.isArray(s) ? s : []);
    const i = await myInvoices(user.userId); if (Array.isArray(i)) setInv(i);
    const p = await myPayments(user.userId); if (Array.isArray(p)) setPays(p);
    const pl = await getPlans();
    if (Array.isArray(pl)) {
      const m = {}; pl.forEach((x) => { m[x.code] = x; }); setPM(m);
    }
  };
  useEffect(() => { load(); }, []);

  const askAction = (fn, id, label, planName) => {
    setConfirm({
      title: `${label} subscription?`,
      message: `Are you sure you want to ${label.toLowerCase()} your ${planName} subscription?`,
      confirmLabel: label === 'Cancel' ? 'yes' : label,
      cancelLabel: label === 'Cancel' ? 'NO' : 'Cancel',
      run: async () => {
        setConfirm(null);
        const res = await fn(id);
        if (res.error) setError(res.error);
        load();
      },
    });
  };

  const upgrade = async (sub) => {
    const next = sub.planCode === 'BASIC' ? 'PRO' : sub.planCode === 'PRO' ? 'PREMIUM' : null;
    if (!next) return setError('Already on Premium — cannot upgrade further.');
    const nextPlan = plansMap[next];
    setConfirm({
      title: 'Upgrade subscription?',
      message: `Upgrade from ${plansMap[sub.planCode]?.name || sub.planCode} to ${nextPlan?.name || next}?`,
      confirmLabel: 'Upgrade',
      run: async () => {
        setConfirm(null);
        const res = await changeSub(sub.subId, next, sub.billingCycle);
        if (res.error) setError(res.error);
        load();
      },
    });
  };

  const planLabel = (code) => {
    const p = plansMap[code];
    return p ? `${p.name} (${p.quality}, ${p.screens} screen${p.screens > 1 ? 's' : ''})` : code;
  };
  const priceLabel = (sub) => {
    const p = plansMap[sub.planCode];
    if (!p) return '';
    const amt = sub.billingCycle === 'YEARLY' ? p.yearlyInr : p.monthlyInr;
    return `₹${amt}/${sub.billingCycle === 'YEARLY' ? 'yr' : 'mo'}`;
  };

  const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.firstName;
  const initials = (fullName || 'U').split(' ').map((s) => s[0]).join('').slice(0, 2).toUpperCase();
  const activeSubs = subs.filter((s) => s.status !== 'CANCELLED' && s.status !== 'EXPIRED');
  const totalSpend = payments.filter((p) => (p.status || '').toLowerCase() === 'success')
    .reduce((sum, p) => sum + (Number(p.amount) || 0), 0);

  return (
    <div className="page account-page">
      <div className="account-header">
        <div className="avatar">{initials}</div>
        <div className="account-meta">
          <h2>{fullName}</h2>
          <div className="account-sub">{user.email}{user.age ? ` · Age ${user.age}` : ''}</div>
        </div>
      </div>

      <div className="stat-grid">
        <div className="stat-card">
          <div className="stat-label">Active Plans</div>
          <div className="stat-value">{activeSubs.length}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Total Invoices</div>
          <div className="stat-value">{invoices.length}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Total Spent</div>
          <div className="stat-value">₹{totalSpend}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Payments</div>
          <div className="stat-value">{payments.length}</div>
        </div>
      </div>

      <div className="section-tabs">
        <button className={tab === 'subs' ? 'on' : ''} onClick={() => setTab('subs')}>Subscriptions</button>
        <button className={tab === 'invoices' ? 'on' : ''} onClick={() => setTab('invoices')}>Invoices</button>
        <button className={tab === 'payments' ? 'on' : ''} onClick={() => setTab('payments')}>Payment History</button>
      </div>

      {tab === 'subs' && (
        <div className="section-card">
          {subs.length === 0 ? (
            <div className="empty-state">
              <p>No subscriptions yet.</p>
              <p className="muted small">Visit the Plans tab to start your subscription.</p>
            </div>
          ) : (
            <table>
              <thead><tr><th>Plan</th><th>Price</th><th>Cycle</th><th>Status</th><th>Started</th><th>Expires</th><th>Actions</th></tr></thead>
              <tbody>
                {subs.map((s) => (
                  <tr key={s.subId}>
                    <td>{planLabel(s.planCode)}</td>
                    <td>{priceLabel(s)}</td>
                    <td>{s.billingCycle}</td>
                    <td><span className={'badge ' + s.status.toLowerCase()}>{s.status}</span></td>
                    <td>{s.startedAt?.slice(0, 10)}</td>
                    <td>{s.expiresAt?.slice(0, 10)}</td>
                    <td className="actions-cell">
                      <button disabled={s.status === 'CANCELLED'} onClick={() => askAction(cancelSub, s.subId, 'Cancel', plansMap[s.planCode]?.name || s.planCode)}>Cancel</button>
                      <button onClick={() => askAction(renewSub, s.subId, 'Renew', plansMap[s.planCode]?.name || s.planCode)}>Renew</button>
                      <button disabled={s.planCode === 'PREMIUM'} onClick={() => upgrade(s)}>Upgrade</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {tab === 'invoices' && (
        <div className="section-card">
          {invoices.length === 0 ? (
            <div className="empty-state"><p>No invoices yet.</p></div>
          ) : (
            <table>
              <thead><tr><th>Invoice</th><th>Plan</th><th>Amount</th><th>Issued</th></tr></thead>
              <tbody>
                {invoices.map((i) => {
                  const sub = subs.find((s) => s.subId === i.subId);
                  return (
                    <tr key={i.invoiceId}>
                      <td className="mono">#{String(i.invoiceId).slice(0, 8)}</td>
                      <td>{sub ? planLabel(sub.planCode) : '—'}</td>
                      <td>₹{i.amount}</td>
                      <td>{i.issuedAt?.slice(0, 10)}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      )}

      {tab === 'payments' && (
        <div className="section-card">
          {payments.length === 0 ? (
            <div className="empty-state"><p>No payments yet.</p></div>
          ) : (
            <table>
              <thead><tr><th>Date</th><th>Amount</th><th>Card</th><th>Status</th></tr></thead>
              <tbody>
                {payments.map((p) => (
                  <tr key={p.paymentId}>
                    <td>{p.createdAt?.slice(0, 10)}</td>
                    <td>₹{p.amount}</td>
                    <td className="mono">**** {p.cardLast4}</td>
                    <td><span className={'badge ' + p.status.toLowerCase()}>{p.status}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      <ConfirmModal
        open={!!confirm}
        title={confirm?.title}
        message={confirm?.message}
        confirmLabel={confirm?.confirmLabel}
        cancelLabel={confirm?.cancelLabel}
        onConfirm={() => confirm?.run()}
        onClose={() => setConfirm(null)}
      />
      <ErrorModal message={error} onClose={() => setError(null)} />
    </div>
  );
}
