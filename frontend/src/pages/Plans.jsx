import { useEffect, useState } from 'react';
import { getPlans, subscribe } from '../api.js';
import ErrorModal from '../components/ErrorModal.jsx';
import PaymentFlow from '../components/PaymentFlow.jsx';

export default function Plans({ user, onSubscribed }) {
  const [plans, setPlans]   = useState([]);
  const [cycle, setCycle]   = useState('MONTHLY');
  const [error, setError]   = useState(null);
  const [success, setOk]    = useState(null);
  const [pending, setPending] = useState(null);

  useEffect(() => { (async () => {
    const res = await getPlans();
    if (res.error) setError(res.error); else setPlans(res);
  })(); }, []);

  const handlePay = async (last4) => {
    setOk(null);
    const res = await subscribe(user.userId, pending.code, cycle, last4);
    if (res.error) { setPending(null); return setError(res.error); }
    setOk(`${user.firstName}, you're subscribed to ${pending.name}. Status: ${res.status}. Expires ${res.expiresAt?.slice(0, 10)}.`);
    setPending(null);
    onSubscribed && onSubscribed();
  };

  return (
    <div className="page">
      <h2>Choose Your Plan</h2>
      <div className="cycle-toggle">
        <button className={cycle === 'MONTHLY' ? 'on' : ''} onClick={() => setCycle('MONTHLY')}>Monthly</button>
        <button className={cycle === 'YEARLY'  ? 'on' : ''} onClick={() => setCycle('YEARLY')}>Yearly (save 15%)</button>
      </div>
      <div className="plan-grid">
        {plans.map((p) => (
          <div className="plan-card" key={p.code}>
            <div className="plan-name">{p.name}</div>
            <div className="plan-price">
              ₹{cycle === 'YEARLY' ? p.yearlyInr : p.monthlyInr}
              <span>/{cycle === 'YEARLY' ? 'year' : 'month'}</span>
            </div>
            <ul>
              <li>{p.quality} quality</li>
              <li>{p.screens} screen{p.screens > 1 ? 's' : ''}</li>
              <li>{p.trialDays}-day free trial</li>
              {cycle === 'YEARLY' && <li>Save {p.yearlyDiscountPct}% yearly</li>}
            </ul>
            <button onClick={() => setPending(p)}>Subscribe</button>
          </div>
        ))}
      </div>
      {success && <div className="success">{success}</div>}
      <PaymentFlow user={user} plan={pending} cycle={cycle} onConfirm={handlePay} onClose={() => setPending(null)} />
      <ErrorModal message={error} onClose={() => setError(null)} />
    </div>
  );
}
