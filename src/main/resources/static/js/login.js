const LOGIN_API = '/login';

document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('login-form');
  form.addEventListener('submit', submitLogin);
});

function submitLogin(event) {
  event.preventDefault();
  const name = document.getElementById('login-name').value.trim();
  const password = document.getElementById('login-password').value;
  const errorBox = document.getElementById('login-error');
  errorBox.classList.add('d-none');

  if (!name || !password) {
    showLoginError('이름과 비밀번호를 모두 입력해주세요.');
    return;
  }

  apiFetch(LOGIN_API, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({ name, password })
  })
    .then(() => {
      const params = new URLSearchParams(window.location.search);
      window.location.href = params.get('redirect') || '/';
    })
    .catch(err => showLoginError(err && err.message ? err.message : '로그인에 실패했습니다.'));
}

function showLoginError(message) {
  const errorBox = document.getElementById('login-error');
  errorBox.textContent = message;
  errorBox.classList.remove('d-none');
}
