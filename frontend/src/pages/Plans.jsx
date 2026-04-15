import { useEffect, useState } from 'react';
import { getPlans, subscribe } from '../api.js';
import ErrorModal from '../components/ErrorModal.jsx';

export default function Plans({ user, onSubscribed }) {
  const [plans, setPlans]   = useState([]);
  const [cycle, setCycle]   = useState('MONTHLY');
  const [card, setCard]     = useState('1234');
  const [error, setError]   = useState(null);
  const [success, setOk]    = useState(null);

  useEffect(() => { (async () => {
    const res = await getPlans();
    if (res.error) setError(res.error); else setPlans(res);
  })(); }, []);

  const pick = async (code) => {
    setOk(null);
    const res = await subscribe(user.userId, code, cycle, card);
    if (res.error) return setError(res.error);
    const plan = plans.find((p) => p.code === code);
    setOk(`${user.firstName}, you're subscribed to ${plan?.name || code}. Status: ${res.status}. Expires ${res.expiresAt?.slice(0, 10)}.`);
    onSubscribed && onSubscribed();
  };

  return (
    <div className="page">
      <h2>Choose Your Plan</h2>
      <div className="cycle-toggle">
        <button className={cycle === 'MONTHLY' ? 'on' : ''} onClick={() => setCycle('MONTHLY')}>Monthly</button>
        <button className={cycle === 'YEARLY'  ? 'on' : ''} onClick={() => setCycle('YEARLY')}>Yearly (save 15%)</button>
      </div>
      <label className="card-input">Test card last-4 (use 0000 to simulate failure)
        <input value={card} onChange={(e) => setCard(e.target.value)} />
      </label>
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
            <button onClick={() => pick(p.code)}>Subscribe</button>
          </div>
        ))}
      </div>
      {success && <div className="success">{success}</div>}
      <ErrorModal message={error} onClose={() => setError(null)} />
    </div>
  );
}
