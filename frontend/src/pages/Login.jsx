import { useState } from 'react';
import { login, signup, getProfile, saveSession } from '../api.js';
import ErrorModal from '../components/ErrorModal.jsx';

const EMAIL_RE = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

export default function Login({ onSuccess }) {
  const [mode, setMode]   = useState('login');
  const [firstName, setFirst] = useState('');
  const [lastName, setLast]   = useState('');
  const [age, setAge]         = useState('');
  const [email, setEmail] = useState('');
  const [pass, setPass]   = useState('');
  const [error, setError] = useState(null);

  const submit = async (e) => {
    e.preventDefault();
    if (!email || !pass) return setError('Email and password are required.');
    if (!EMAIL_RE.test(email)) return setError('Please enter a valid email address.');
    if (mode === 'signup') {
      if (!firstName.trim() || !lastName.trim()) return setError('First and last name are required.');
      if (!age || isNaN(age) || Number(age) < 13) return setError('Please enter a valid age (13+).');
    }

    const res = mode === 'signup'
      ? await signup(firstName.trim(), lastName.trim(), Number(age), email, pass)
      : await login(email, pass);
    if (res.error) return setError(res.error);

    const profile = await getProfile(res.userId);
    const base = profile.error
      ? { userId: res.userId, firstName: res.firstName || firstName, lastName: res.lastName || lastName, email: res.email }
      : profile;
    const user = { ...base, role: res.role || 'USER' };
    saveSession(res.token, user);
    onSuccess(user);
  };

  return (
    <div className="auth-wrap">
      <form className="auth-card" onSubmit={submit}>
        <h1>NETFLIX<span className="dot">.</span>sub</h1>
        <h2>{mode === 'signup' ? 'Create your account' : 'Sign in'}</h2>
        {mode === 'signup' && (
          <>
            <div className="row-2">
              <label>First name<input value={firstName} onChange={(e) => setFirst(e.target.value)} /></label>
              <label>Last name<input value={lastName} onChange={(e) => setLast(e.target.value)} /></label>
            </div>
            <label>Age<input type="number" min="13" value={age} onChange={(e) => setAge(e.target.value)} /></label>
          </>
        )}
        <label>Email<input value={email} onChange={(e) => setEmail(e.target.value)} /></label>
        <label>Password<input type="password" value={pass} onChange={(e) => setPass(e.target.value)} /></label>
        <button type="submit">{mode === 'signup' ? 'Sign up' : 'Log in'}</button>
        <p className="muted">
          {mode === 'signup' ? 'Already have an account?' : 'New here?'}{' '}
          <a onClick={() => setMode(mode === 'signup' ? 'login' : 'signup')}>
            {mode === 'signup' ? 'Log in' : 'Sign up'}
          </a>
        </p>
        {mode === 'login' && (
          <p className="muted">
            Admin?{' '}
            <a
              onClick={(e) => {
                e.preventDefault();
                setMode('login');
                setEmail('admin@gmail.com');
                setPass('admin@123');
              }}
              href="#admin-login"
            >
              Log in as admin
            </a>
          </p>
        )}
        <ErrorModal message={error} onClose={() => setError(null)} />
      </form>
    </div>
  );
}
