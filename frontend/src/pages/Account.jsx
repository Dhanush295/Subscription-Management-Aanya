import { useEffect, useState } from 'react';
import { mySubs, cancelSub, renewSub, changeSub, myInvoices, myPayments, getPlans } from '../api.js';
import ErrorModal from '../components/ErrorModal.jsx';

export default function Account({ user }) {
  const [subs, setSubs]     = useState([]);
  const [invoices, setInv]  = useState([]);
  const [payments, setPays] = useState([]);
  const [plansMap, setPM]   = useState({});
  const [error, setError]   = useState(null);

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

  const act = async (fn, id, label) => {
    if (!window.confirm(`${label} this subscription?`)) return;
    const res = await fn(id);
    if (res.error) setError(res.error);
    load();
  };

  const upgrade = async (sub) => {
    const next = sub.planCode === 'BASIC' ? 'PRO' : sub.planCode === 'PRO' ? 'PREMIUM' : null;
    if (!next) return setError('Already on Premium — cannot upgrade further.');
    const res = await changeSub(sub.subId, next, sub.billingCycle);
    if (res.error) setError(res.error);
    load();
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

  const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim();

  return (
    <div className="page">
      <h2>My Account</h2>
      <div className="card">
        <div><strong>Name:</strong> {fullName || user.firstName}</div>
        <div><strong>Email:</strong> {user.email}</div>
        {user.age ? <div><strong>Age:</strong> {user.age}</div> : null}
      </div>

      <h3>My Subscriptions</h3>
      <table>
        <thead><tr><th>Member</th><th>Plan</th><th>Price</th><th>Cycle</th><th>Status</th><th>Started</th><th>Expires</th><th>Actions</th></tr></thead>
        <tbody>
          {subs.length === 0 && <tr><td colSpan="8" className="muted">No subscriptions yet. Visit Plans to start.</td></tr>}
          {subs.map((s) => (
            <tr key={s.subId}>
              <td>{fullName || user.firstName}</td>
              <td>{planLabel(s.planCode)}</td>
              <td>{priceLabel(s)}</td>
              <td>{s.billingCycle}</td>
              <td><span className={'badge ' + s.status.toLowerCase()}>{s.status}</span></td>
              <td>{s.startedAt?.slice(0, 10)}</td>
              <td>{s.expiresAt?.slice(0, 10)}</td>
              <td>
                <button disabled={s.status === 'CANCELLED'} onClick={() => act(cancelSub, s.subId, 'Cancel')}>Cancel</button>
                <button onClick={() => act(renewSub, s.subId, 'Renew')}>Renew</button>
                <button disabled={s.planCode === 'PREMIUM'} onClick={() => upgrade(s)}>Upgrade</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <h3>Invoices</h3>
      <table>
        <thead><tr><th>Member</th><th>Plan</th><th>Amount</th><th>Issued</th></tr></thead>
        <tbody>
          {invoices.length === 0 && <tr><td colSpan="4" className="muted">No invoices yet.</td></tr>}
          {invoices.map((i) => {
            const sub = subs.find((s) => s.subId === i.subId);
            return (
              <tr key={i.invoiceId}>
                <td>{fullName || user.firstName}</td>
                <td>{sub ? planLabel(sub.planCode) : '—'}</td>
                <td>₹{i.amount}</td>
                <td>{i.issuedAt?.slice(0, 10)}</td>
              </tr>
            );
          })}
        </tbody>
      </table>

      <h3>Payment History</h3>
      <table>
        <thead><tr><th>Member</th><th>Amount</th><th>Card</th><th>Status</th><th>When</th></tr></thead>
        <tbody>
          {payments.length === 0 && <tr><td colSpan="5" className="muted">No payments yet.</td></tr>}
          {payments.map((p) => (
            <tr key={p.paymentId}>
              <td>{fullName || user.firstName}</td>
              <td>₹{p.amount}</td>
              <td>**** {p.cardLast4}</td>
              <td><span className={'badge ' + p.status.toLowerCase()}>{p.status}</span></td>
              <td>{p.createdAt?.slice(0, 10)}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <ErrorModal message={error} onClose={() => setError(null)} />
    </div>
  );
}
