const LOGIN_CHECK_API = '/login/check';
const LOGOUT_API = '/logout';

document.addEventListener('DOMContentLoaded', () => {
  const toggle = document.querySelector('.nav-toggle');
  const nav = document.querySelector('.nav');
  if (toggle && nav) {
    toggle.addEventListener('click', () => nav.classList.toggle('open'));
  }

  renderAuthArea();
});

function renderAuthArea() {
  const topbarInner = document.querySelector('.topbar-inner');
  if (!topbarInner) return;

  const slot = document.createElement('div');
  slot.className = 'auth-slot';
  topbarInner.appendChild(slot);

  fetch(LOGIN_CHECK_API, { credentials: 'same-origin' })
    .then(res => {
      if (res.status === 200) return res.json();
      return null;
    })
    .then(user => {
      if (user && user.name) {
        renderLoggedIn(slot, user.name);
        ensureMyPageLink();
      } else {
        renderLoggedOut(slot);
      }
    })
    .catch(() => renderLoggedOut(slot));
}

function renderLoggedIn(slot, name) {
  slot.innerHTML = '';
  const label = document.createElement('span');
  label.className = 'auth-user';
  label.innerHTML = `<i class="fas fa-user-circle"></i> <strong>${escapeHtml(name)}</strong>님`;

  const logoutBtn = document.createElement('button');
  logoutBtn.type = 'button';
  logoutBtn.className = 'btn btn-ghost btn-sm';
  logoutBtn.textContent = '로그아웃';
  logoutBtn.addEventListener('click', logout);

  slot.appendChild(label);
  slot.appendChild(logoutBtn);
}

function renderLoggedOut(slot) {
  slot.innerHTML = '';
  const signupLink = document.createElement('a');
  signupLink.href = '/signup';
  signupLink.className = 'btn btn-ghost btn-sm';
  signupLink.textContent = '회원가입';

  const loginLink = document.createElement('a');
  loginLink.href = '/login';
  loginLink.className = 'btn btn-secondary btn-sm';
  loginLink.innerHTML = '<i class="fas fa-sign-in-alt"></i> 로그인';

  slot.appendChild(signupLink);
  slot.appendChild(loginLink);
}

function ensureMyPageLink() {
  const nav = document.querySelector('.nav');
  if (!nav) return;
  if (nav.querySelector('a[href="/my-reservations"]')) return;
  const link = document.createElement('a');
  link.href = '/my-reservations';
  link.textContent = '마이페이지';
  if (window.location.pathname.startsWith('/my-reservations')) {
    link.classList.add('active');
  }
  nav.appendChild(link);
}

function logout() {
  fetch(LOGOUT_API, { method: 'POST', credentials: 'same-origin' })
    .finally(() => { window.location.href = '/'; });
}

function escapeHtml(str) {
  return String(str).replace(/[&<>"']/g, ch => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  })[ch]);
}
