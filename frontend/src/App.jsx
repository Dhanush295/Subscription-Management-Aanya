import { useState } from 'react';
import Login from './pages/Login.jsx';
import Browse from './pages/Browse.jsx';
import Plans from './pages/Plans.jsx';
import Account from './pages/Account.jsx';
import Admin from './pages/Admin.jsx';
import { currentUser, clearSession, logout } from './api.js';

const TABS = [
  { id: 'browse',  label: 'Browse' },
  { id: 'plans',   label: 'Plans' },
  { id: 'account', label: 'My Account' },
];

export default function App() {
  const [user, setUser] = useState(currentUser());
  const [tab, setTab]   = useState('browse');

  if (!user) return <Login onSuccess={setUser} />;

  const handleLogout = async () => {
    await logout();
    clearSession();
    setUser(null);
  };

  const greet = user.firstName || (user.fullName ? user.fullName.split(' ')[0] : 'there');

  if (user.role === 'ADMIN') {
    return (
      <div className="app">
        <header className="top-bar">
          <div className="brand">NETFLIX<span className="dot">.</span>sub <span className="muted">admin</span></div>
          <nav className="tabs">
            <button className="tab on">Admin</button>
          </nav>
          <div className="user-info">
            <span>Hi, {greet}</span>
            <button onClick={handleLogout}>Logout</button>
          </div>
        </header>
        <main><Admin /></main>
      </div>
    );
  }

  return (
    <div className="app">
      <header className="top-bar">
        <div className="brand">NETFLIX<span className="dot">.</span>sub</div>
        <nav className="tabs">
          {TABS.map((t) => (
            <button key={t.id} className={tab === t.id ? 'tab on' : 'tab'} onClick={() => setTab(t.id)}>
              {t.label}
            </button>
          ))}
        </nav>
        <div className="user-info">
          <span>Hi, {greet}</span>
          <button onClick={handleLogout}>Logout</button>
        </div>
      </header>
      <main>
        {tab === 'browse'  && <Browse />}
        {tab === 'plans'   && <Plans user={user} onSubscribed={() => setTab('account')} />}
        {tab === 'account' && <Account user={user} />}
      </main>
    </div>
  );
}
