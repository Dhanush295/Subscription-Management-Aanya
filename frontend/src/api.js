const BASE = '/api';

function token() { return localStorage.getItem('token'); }

async function req(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  const t = token();
  if (t) headers['Authorization'] = 'Bearer ' + t;
  const res = await fetch(BASE + path, { ...options, headers });
  const text = await res.text();
  try { return JSON.parse(text); } catch { return { error: text || `HTTP ${res.status}` }; }
}

export const signup = (firstName, lastName, age, email, password) =>
  req('/auth/signup', { method: 'POST', body: JSON.stringify({ firstName, lastName, age, email, password }) });
export const login  = (email, password) =>
  req('/auth/login',  { method: 'POST', body: JSON.stringify({ email, password }) });
export const logout = () => req('/auth/logout', { method: 'POST' });

export const getProfile = (userId) => req(`/users/${userId}`);

export const getPlans  = ()                      => req('/plans');
export const getPlan   = (code)                  => req(`/plans/${code}`);

export const getMovies = (language, genre) => {
  const q = new URLSearchParams();
  if (language) q.set('language', language);
  if (genre)    q.set('genre', genre);
  const qs = q.toString();
  return req('/movies' + (qs ? '?' + qs : ''));
};

export const mySubs    = (userId) => req(`/subscriptions/user/${userId}`);
export const subscribe = (userId, planCode, billingCycle, cardLast4) =>
  req('/subscriptions', { method: 'POST', body: JSON.stringify({ userId, planCode, billingCycle, cardLast4 }) });
export const cancelSub = (subId)            => req(`/subscriptions/${subId}/cancel`, { method: 'PUT' });
export const changeSub = (subId, planCode, billingCycle) =>
  req(`/subscriptions/${subId}/change`, { method: 'PUT', body: JSON.stringify({ planCode, billingCycle }) });
export const renewSub  = (subId)            => req(`/subscriptions/${subId}/renew`, { method: 'PUT' });

export const myInvoices = (userId) => req(`/invoices/user/${userId}`);
export const myPayments = (userId) => req(`/payments/user/${userId}`);

export const adminListSubs   = ()       => req('/admin/subscriptions');
export const adminCancelSub  = (subId)  => req(`/admin/subscriptions/${subId}/cancel`, { method: 'PUT' });
export const adminDeleteSub  = (subId)  => req(`/admin/subscriptions/${subId}`, { method: 'DELETE' });
export const adminListUsers  = ()       => req('/admin/users');
export const adminDeleteUser = (userId) => req(`/admin/users/${userId}`, { method: 'DELETE' });
export const adminListPlans  = ()       => req('/admin/plans');

export function saveSession(token, user) {
  localStorage.setItem('token', token);
  localStorage.setItem('user',  JSON.stringify(user));
}
export function clearSession() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
}
export function currentUser() {
  try { return JSON.parse(localStorage.getItem('user')); } catch { return null; }
}
