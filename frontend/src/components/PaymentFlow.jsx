import { useState, useEffect } from 'react';

export default function PaymentFlow({ plan, cycle, onConfirm, onClose, user }) {
  const [step, setStep] = useState('confirm');
  const [processing, setProcessing] = useState(false);
  const [card, setCard] = useState({
    name: user ? `${user.firstName || ''} ${user.lastName || ''}`.trim() : '',
    number: '4242 4242 4242 1234',
    expiry: '12/29',
    cvv: '123',
  });

  useEffect(() => {
    const onKey = (e) => e.key === 'Escape' && !processing && onClose();
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [processing, onClose]);

  if (!plan) return null;
  const amount = cycle === 'YEARLY' ? plan.yearlyInr : plan.monthlyInr;
  const period = cycle === 'YEARLY' ? 'year' : 'month';

  const handlePay = async () => {
    setProcessing(true);
    const last4 = card.number.replace(/\s/g, '').slice(-4);
    await new Promise((r) => setTimeout(r, 1200));
    await onConfirm(last4);
    setProcessing(false);
  };

  return (
    <div className="modal-overlay" onClick={() => !processing && onClose()}>
      <div className="modal payment-modal" onClick={(e) => e.stopPropagation()}>
        {step === 'confirm' && (
          <>
            <h3>Confirm Subscription</h3>
            <p className="pay-line">You're about to subscribe to:</p>
            <div className="pay-summary">
              <div className="pay-summary-row"><span>Plan</span><strong>{plan.name}</strong></div>
              <div className="pay-summary-row"><span>Quality</span><strong>{plan.quality}</strong></div>
              <div className="pay-summary-row"><span>Screens</span><strong>{plan.screens}</strong></div>
              <div className="pay-summary-row"><span>Billing</span><strong>{cycle}</strong></div>
              <div className="pay-summary-row total"><span>Total</span><strong>₹{amount}/{period}</strong></div>
            </div>
            <p className="muted small">A {plan.trialDays}-day free trial applies before billing starts.</p>
            <div className="pay-actions">
              <button className="btn-secondary" onClick={onClose}>Cancel</button>
              <button className="btn-primary" onClick={() => setStep('payment')}>Continue to Payment</button>
            </div>
          </>
        )}

        {step === 'payment' && (
          <>
            <h3>Payment Details</h3>
            <div className="pay-amount">₹{amount}<span>/{period}</span></div>
            <div className="pay-form">
              <label>Cardholder Name
                <input value={card.name} onChange={(e) => setCard({ ...card, name: e.target.value })} disabled={processing} />
              </label>
              <label>Card Number
                <input value={card.number} onChange={(e) => setCard({ ...card, number: e.target.value })} disabled={processing} />
              </label>
              <div className="pay-row">
                <label>Expiry
                  <input value={card.expiry} onChange={(e) => setCard({ ...card, expiry: e.target.value })} disabled={processing} />
                </label>
                <label>CVV
                  <input value={card.cvv} onChange={(e) => setCard({ ...card, cvv: e.target.value })} disabled={processing} />
                </label>
              </div>
              <div className="pay-brands">VISA · Mastercard · RuPay · Amex</div>
            </div>
            <div className="pay-actions">
              <button className="btn-secondary" onClick={() => setStep('confirm')} disabled={processing}>Back</button>
              <button className="btn-primary" onClick={handlePay} disabled={processing}>
                {processing ? 'Processing…' : `Pay ₹${amount}`}
              </button>
            </div>
            <p className="muted small lock">🔒 This is a demo. No real payment will be charged.</p>
          </>
        )}
      </div>
    </div>
  );
}
