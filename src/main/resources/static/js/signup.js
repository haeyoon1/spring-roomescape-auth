const SIGNUP_API = '/signup';

document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('signup-form');
  form.addEventListener('submit', submitSignup);
});

function submitSignup(event) {
  event.preventDefault();
  const name = document.getElementById('signup-name').value.trim();
  const password = document.getElementById('signup-password').value;
  const errorBox = document.getElementById('signup-error');
  errorBox.classList.add('d-none');

  if (!name || !password) {
    showSignupError('이름과 비밀번호를 모두 입력해주세요.');
    return;
  }

  apiFetch(SIGNUP_API, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({ name, password })
  })
    .then(() => {
      alert('가입이 완료되었습니다.');
      const params = new URLSearchParams(window.location.search);
      window.location.href = params.get('redirect') || '/';
    })
    .catch(err => showSignupError(err && err.message ? err.message : '가입에 실패했습니다.'));
}

function showSignupError(message) {
  const errorBox = document.getElementById('signup-error');
  errorBox.textContent = message;
  errorBox.classList.remove('d-none');
}
