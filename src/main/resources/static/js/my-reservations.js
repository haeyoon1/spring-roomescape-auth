const MY_RESERVATIONS_API = '/reservations/mine';
const TIMES_API = '/times';

let timesCache = [];

document.addEventListener('DOMContentLoaded', () => {
  bootstrap();
});

function bootstrap() {
  apiFetch('/login/check')
    .then(user => {
      if (user && user.name) {
        document.getElementById('my-greeting').textContent =
          `${user.name}님의 예약 내역입니다.`;
      }
      return apiFetch(TIMES_API);
    })
    .then(times => { timesCache = times || []; })
    .then(loadMyReservations)
    .catch(handleApiError);
}

function loadMyReservations() {
  return apiFetch(MY_RESERVATIONS_API)
    .then(data => renderReservations((data && data.reservations) || []))
    .catch(handleApiError);
}

function renderReservations(reservations) {
  const tbody = document.getElementById('reservation-table-body');
  const empty = document.getElementById('reservation-empty');
  tbody.innerHTML = '';

  if (reservations.length === 0) {
    empty.classList.remove('d-none');
    return;
  }
  empty.classList.add('d-none');

  reservations.forEach(r => tbody.appendChild(buildRow(r)));
}

function buildRow(r) {
  const row = document.createElement('tr');
  row.dataset.id = r.id;

  row.appendChild(cell(r.id));
  row.appendChild(cell(r.themeName || '-'));
  row.appendChild(cell(r.date));
  row.appendChild(cell(formatTime(r.time)));

  const actions = document.createElement('td');
  actions.className = 'actions-cell';
  actions.appendChild(button('수정', 'btn btn-primary btn-sm', () => startEdit(row, r)));
  actions.appendChild(button('삭제', 'btn btn-danger btn-sm', () => deleteReservation(r.id, row)));
  row.appendChild(actions);

  return row;
}

function startEdit(row, r) {
  const cells = row.children;
  cells[2].innerHTML = '';
  const dateInput = document.createElement('input');
  dateInput.type = 'date';
  dateInput.className = 'form-control';
  dateInput.value = r.date;
  cells[2].appendChild(dateInput);

  cells[3].innerHTML = '';
  const timeSelect = document.createElement('select');
  timeSelect.className = 'form-control';
  timesCache.forEach(t => {
    const opt = document.createElement('option');
    opt.value = t.id;
    opt.textContent = formatTime(t.startAt);
    if (t.startAt === r.time) opt.selected = true;
    timeSelect.appendChild(opt);
  });
  cells[3].appendChild(timeSelect);

  const actions = cells[4];
  actions.innerHTML = '';
  actions.className = 'actions-cell';
  actions.appendChild(button('저장', 'btn btn-success btn-sm', () => {
    saveEdit(r.id, dateInput.value, parseInt(timeSelect.value));
  }));
  actions.appendChild(button('취소', 'btn btn-ghost btn-sm', () => loadMyReservations()));
}

function saveEdit(id, date, timeId) {
  if (!date || !timeId) {
    alert('날짜와 시간을 선택해주세요.');
    return;
  }
  apiFetch(`/reservation/${id}`, {
    method: 'PATCH',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({ date, timeId })
  })
    .then(() => {
      alert('예약이 수정되었습니다.');
      loadMyReservations();
    })
    .catch(handleApiError);
}

function deleteReservation(id, row) {
  if (!confirm('이 예약을 삭제하시겠습니까?')) return;
  apiFetch(`/reservation/${id}`, { method: 'DELETE' })
    .then(() => {
      row.remove();
      const tbody = document.getElementById('reservation-table-body');
      if (!tbody.children.length) {
        document.getElementById('reservation-empty').classList.remove('d-none');
      }
    })
    .catch(handleApiError);
}

function cell(text) {
  const td = document.createElement('td');
  td.textContent = text;
  return td;
}

function button(label, className, handler) {
  const b = document.createElement('button');
  b.textContent = label;
  b.className = className;
  b.addEventListener('click', handler);
  return b;
}

function formatTime(value) {
  if (!value) return '';
  const [h, m] = value.split(':');
  return `${h}:${m}`;
}
