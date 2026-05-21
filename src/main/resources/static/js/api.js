async function apiFetch(url, options = {}) {
  const res = await fetch(url, { credentials: 'same-origin', ...options });
  if (res.status === 204) return null;
  const text = await res.text();
  const body = text ? JSON.parse(text) : null;
  if (!res.ok) {
    const message = body && body.message ? body.message : `요청에 실패했습니다 (${res.status}).`;
    const err = new Error(message);
    err.status = res.status;
    throw err;
  }
  return body;
}

function showError(err) {
  alert(err && err.message ? err.message : '알 수 없는 오류가 발생했습니다.');
}

function redirectToLoginIfUnauthorized(err) {
  if (err && err.status === 401) {
    const redirect = encodeURIComponent(window.location.pathname);
    window.location.href = `/login?redirect=${redirect}`;
    return true;
  }
  return false;
}

function showForbiddenIfDenied(err) {
  if (err && err.status === 403) {
    alert(err.message || '해당 작업을 수행할 권한이 없습니다.');
    return true;
  }
  return false;
}

function handleApiError(err) {
  if (redirectToLoginIfUnauthorized(err)) return;
  if (showForbiddenIfDenied(err)) return;
  showError(err);
}