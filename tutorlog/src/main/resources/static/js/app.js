const API_BASE = '';

// ── State ─────────────────────────────────────────────────
const state = {
    token: localStorage.getItem('token'),
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    view: 'home',
    params: {}
};

// ── API Client ────────────────────────────────────────────
async function api(method, endpoint, body) {
    const url = `${API_BASE}${endpoint}`;
    const opts = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (state.token) opts.headers['Authorization'] = `Bearer ${state.token}`;
    if (body) opts.body = JSON.stringify(body);

    const res = await fetch(url, opts);
    if (res.status === 204) return null;

    let data;
    try { data = await res.json(); } catch { data = null; }

    if (!res.ok) {
        const msg = data?.message || `${res.status} ${res.statusText}`;
        throw new Error(msg);
    }
    return data;
}

const get = (ep) => api('GET', ep);
const post = (ep, body) => api('POST', ep, body);
const put = (ep, body) => api('PUT', ep, body);
const del = (ep) => api('DELETE', ep);

// ── Auth helpers ─────────────────────────────────────────
function isAuth() { return !!state.token; }
function isTutor() { return state.user?.role === 'tutor' || state.user?.role === 'ROLE_TUTOR'; }
function isAdmin() { return state.user?.role === 'ROLE_ADMIN'; }

function setAuth(token, user) {
    state.token = token;
    state.user = user;
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
    renderNav();
}

function logout() {
    state.token = null;
    state.user = null;
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    renderNav();
    navigate('home');
}

// ── Router ───────────────────────────────────────────────
function navigate(view, params = {}) {
    state.view = view;
    state.params = params;
    window.scrollTo(0, 0);
    render();
}

window.addEventListener('popstate', () => {
    // simple hash-based routing if needed later
    render();
});

// ── Render dispatcher ────────────────────────────────────
function render() {
    const app = document.getElementById('app');
    app.innerHTML = '';
    switch (state.view) {
        case 'home': renderHome(app); break;
        case 'login': renderLogin(app); break;
        case 'register': renderRegister(app); break;
        case 'lessons': renderLessons(app); break;
        case 'lesson': renderLesson(app, state.params.id); break;
        case 'create-lesson': renderCreateLesson(app); break;
        case 'edit-lesson': renderEditLesson(app, state.params.id); break;
        case 'subscriptions': renderSubscriptions(app); break;
        case 'profile': renderProfile(app); break;
        case 'tutors': renderTutors(app); break;
        case 'students': renderStudents(app); break;
        default: renderHome(app);
    }
}

function renderNav() {
    const el = document.getElementById('nav-links');
    if (!el) return;

    let html = `
        <a href="#" onclick="navigate('lessons'); return false;">Lessons</a>
        <a href="#" onclick="navigate('tutors'); return false;">Tutors</a>
    `;

    if (isAuth()) {
        html += `<a href="#" onclick="navigate('subscriptions'); return false;">Subscriptions</a>`;
        if (isTutor() || isAdmin()) {
            html += `<a href="#" onclick="navigate('create-lesson'); return false;">New Lesson</a>`;
            html += `<a href="#" onclick="navigate('students'); return false;">Students</a>`;
        }
        html += `
            <a href="#" onclick="navigate('profile'); return false;">${escapeHtml(state.user?.name || 'Profile')}</a>
            <button onclick="logout()">Logout</button>
        `;
    } else {
        html += `
            <a href="#" onclick="navigate('login'); return false;">Login</a>
            <a href="#" onclick="navigate('register'); return false;"><b>Sign Up</b></a>
        `;
    }
    el.innerHTML = html;
}

// ── Views ────────────────────────────────────────────────

function renderHome(container) {
    container.innerHTML = `
        <div class="hero">
            <h1>Learn from the best tutors</h1>
            <p>TutorLog connects students with expert tutors. Browse free lessons or subscribe for premium content.</p>
            <div class="flex gap-1 justify-center">
                <button class="btn btn-primary" onclick="navigate('lessons')">Browse Lessons</button>
                ${!isAuth() ? `<button class="btn btn-secondary" onclick="navigate('register')">Get Started</button>` : ''}
            </div>
        </div>
    `;
}

function renderLogin(container) {
    container.innerHTML = `
        <div class="max-w-md">
            <h2 class="page-title">Login</h2>
            <div id="login-alert"></div>
            <form id="login-form" class="card">
                <div class="form-group">
                    <label>Email</label>
                    <input type="email" id="login-email" required placeholder="you@example.com">
                </div>
                <div class="form-group">
                    <label>Password</label>
                    <input type="password" id="login-password" required placeholder="••••••">
                </div>
                <button type="submit" class="btn btn-primary w-full">Sign In</button>
                <p class="text-sm text-muted mt-2">No account? <a href="#" onclick="navigate('register'); return false;">Sign up</a></p>
            </form>
        </div>
    `;
    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;
        try {
            const data = await post('/api/auth/login', { email, password });
            setAuth(data.token, { id: data.id, name: data.name, email: data.email, role: data.role });
            navigate('lessons');
        } catch (err) {
            showAlert('login-alert', err.message, 'danger');
        }
    });
}

function renderRegister(container) {
    container.innerHTML = `
        <div class="max-w-md">
            <h2 class="page-title">Create an account</h2>
            <div id="reg-alert"></div>
            <form id="reg-form" class="card">
                <div class="form-group">
                    <label>Full Name</label>
                    <input type="text" id="reg-name" required placeholder="Alex Johnson">
                </div>
                <div class="form-group">
                    <label>Email</label>
                    <input type="email" id="reg-email" required placeholder="alex@example.com">
                </div>
                <div class="form-group">
                    <label>Password</label>
                    <input type="password" id="reg-password" required minlength="6" placeholder="At least 6 characters">
                </div>
                <div class="form-group">
                    <label>I want to join as</label>
                    <select id="reg-role">
                        <option value="student">Student</option>
                        <option value="tutor">Tutor</option>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary w-full">Sign Up</button>
                <p class="text-sm text-muted mt-2">Already have an account? <a href="#" onclick="navigate('login'); return false;">Log in</a></p>
            </form>
        </div>
    `;
    document.getElementById('reg-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('reg-name').value;
        const email = document.getElementById('reg-email').value;
        const password = document.getElementById('reg-password').value;
        const role = document.getElementById('reg-role').value;
        try {
            const data = await post('/api/auth/register', { name, email, password, role });
            setAuth(data.token, { id: data.id, name: data.name, email: data.email, role: data.role });
            navigate('lessons');
        } catch (err) {
            showAlert('reg-alert', err.message, 'danger');
        }
    });
}

async function renderLessons(container) {
    container.innerHTML = `
        <div class="page-header">
            <h2 class="page-title">Lessons</h2>
            ${isTutor() || isAdmin() ? `<button class="btn btn-primary" onclick="navigate('create-lesson')">+ New Lesson</button>` : ''}
        </div>
        <div class="search-bar">
            <input type="text" id="lesson-search" placeholder="Search lessons..." onkeydown="if(event.key==='Enter')searchLessons()">
            <button class="btn btn-secondary" onclick="searchLessons()">Search</button>
        </div>
        <div id="lessons-list"><div class="skeleton" style="height:120px"></div></div>
    `;
    try {
        const lessons = await get('/api/lessons');
        const el = document.getElementById('lessons-list');
        if (!lessons || lessons.length === 0) {
            el.innerHTML = '<p class="text-muted">No lessons found.</p>';
            return;
        }
        el.innerHTML = `<div class="card-grid">${lessons.map(l => lessonCard(l)).join('')}</div>`;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

function lessonCard(l) {
    return `
        <div class="card lesson-card" onclick="navigate('lesson',{id:'${l.id}'})" style="cursor:pointer;">
            <div class="flex justify-between items-center">
                <span class="subject">${escapeHtml(l.subject || '')}</span>
                <span class="badge ${l.accessType === 'FREE' ? 'badge-free' : 'badge-premium'}">${l.accessType || 'FREE'}</span>
            </div>
            <div class="title">${escapeHtml(l.title || '')}</div>
            <div class="text-sm text-muted">${escapeHtml(l.tutorName || '')}</div>
            <div class="meta">
                <span>${l.durationMinutes || 0} min</span>
                <span class="rating">★ ${l.rating || 0} (${l.ratingCount || 0})</span>
            </div>
        </div>
    `;
}

async function searchLessons() {
    const q = document.getElementById('lesson-search').value.trim();
    if (!q) { renderLessons(document.getElementById('app')); return; }
    const el = document.getElementById('lessons-list');
    el.innerHTML = '<div class="skeleton" style="height:120px"></div>';
    try {
        const lessons = await get(`/api/lessons/search?q=${encodeURIComponent(q)}`);
        el.innerHTML = lessons && lessons.length
            ? `<div class="card-grid">${lessons.map(l => lessonCard(l)).join('')}</div>`
            : '<p class="text-muted">No lessons match your search.</p>';
    } catch (err) {
        el.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

async function renderLesson(container, id) {
    container.innerHTML = `<div class="max-w-2xl"><div class="skeleton" style="height:200px"></div></div>`;
    try {
        const lesson = await get(`/api/lessons/${id}`);
        const isOwner = isTutor() && lesson.tutorId === state.user?.id;
        container.innerHTML = `
            <div class="max-w-2xl">
                <div class="flex justify-between items-center mb-1">
                    <span class="badge ${lesson.accessType === 'FREE' ? 'badge-free' : 'badge-premium'}">${lesson.accessType}</span>
                    <div class="flex gap-1">
                        ${isOwner || isAdmin() ? `<button class="btn btn-secondary" onclick="navigate('edit-lesson',{id:'${lesson.id}'})">Edit</button>` : ''}
                        ${isOwner || isAdmin() ? `<button class="btn btn-danger" onclick="deleteLesson('${lesson.id}')">Delete</button>` : ''}
                    </div>
                </div>
                <h2 class="page-title" style="margin-bottom:0.25rem;">${escapeHtml(lesson.title)}</h2>
                <div class="text-muted text-sm mb-2">${escapeHtml(lesson.subject)} &middot; ${escapeHtml(lesson.tutorName)} &middot; ${lesson.durationMinutes} min &middot; ${lesson.viewCount} views</div>
                <div class="card mb-2">
                    <div class="mb-1">${lesson.content || '<em>No content available.</em>'}</div>
                </div>
                <div class="flex justify-between items-center">
                    <div class="rating text-lg">★ ${lesson.rating} <span class="text-sm text-muted">(${lesson.ratingCount} ratings)</span></div>
                    <div class="flex gap-1">
                        <select id="rate-stars" class="text-sm">
                            <option value="5">5 stars</option>
                            <option value="4">4 stars</option>
                            <option value="3">3 stars</option>
                            <option value="2">2 stars</option>
                            <option value="1">1 star</option>
                        </select>
                        <button class="btn btn-secondary" onclick="rateLesson('${lesson.id}')">Rate</button>
                    </div>
                </div>
                <p class="text-sm text-muted mt-2"><a href="#" onclick="navigate('lessons'); return false;">&larr; Back to lessons</a></p>
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger max-w-2xl">${err.message}</div>`;
    }
}

async function rateLesson(id) {
    const stars = parseInt(document.getElementById('rate-stars').value, 10);
    try {
        await post(`/api/lessons/${id}/rate`, { stars });
        showAlert(null, 'Rating submitted!', 'success');
        renderLesson(document.getElementById('app'), id);
    } catch (err) {
        showAlert(null, err.message, 'danger');
    }
}

async function deleteLesson(id) {
    if (!confirm('Delete this lesson?')) return;
    try {
        await del(`/api/lessons/${id}`);
        navigate('lessons');
    } catch (err) {
        showAlert(null, err.message, 'danger');
    }
}

function renderCreateLesson(container) {
    if (!isTutor() && !isAdmin()) { navigate('lessons'); return; }
    container.innerHTML = `
        <div class="max-w-md">
            <h2 class="page-title">New Lesson</h2>
            <div id="lesson-alert"></div>
            <form id="lesson-form" class="card">
                <div class="form-group">
                    <label>Title</label>
                    <input type="text" id="l-title" required>
                </div>
                <div class="form-group">
                    <label>Subject</label>
                    <input type="text" id="l-subject" required>
                </div>
                <div class="form-group">
                    <label>Description</label>
                    <input type="text" id="l-description">
                </div>
                <div class="form-group">
                    <label>Content (HTML allowed)</label>
                    <textarea id="l-content" rows="6" required></textarea>
                </div>
                <div class="form-group">
                    <label>Duration (minutes)</label>
                    <input type="number" id="l-duration" min="1" required>
                </div>
                <div class="form-group">
                    <label>Access</label>
                    <select id="l-access">
                        <option value="FREE">Free</option>
                        <option value="PREMIUM">Premium</option>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary w-full">Create Lesson</button>
            </form>
        </div>
    `;
    document.getElementById('lesson-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const body = {
            title: document.getElementById('l-title').value,
            subject: document.getElementById('l-subject').value,
            description: document.getElementById('l-description').value,
            content: document.getElementById('l-content').value,
            durationMinutes: parseInt(document.getElementById('l-duration').value, 10),
            accessType: document.getElementById('l-access').value
        };
        try {
            await post('/api/lessons', body);
            navigate('lessons');
        } catch (err) {
            showAlert('lesson-alert', err.message, 'danger');
        }
    });
}

async function renderEditLesson(container, id) {
    if (!isTutor() && !isAdmin()) { navigate('lessons'); return; }
    container.innerHTML = `<div class="max-w-md"><div class="skeleton" style="height:200px"></div></div>`;
    try {
        const lesson = await get(`/api/lessons/${id}`);
        container.innerHTML = `
            <div class="max-w-md">
                <h2 class="page-title">Edit Lesson</h2>
                <div id="edit-alert"></div>
                <form id="edit-form" class="card">
                    <div class="form-group">
                        <label>Title</label>
                        <input type="text" id="e-title" value="${escapeHtml(lesson.title)}" required>
                    </div>
                    <div class="form-group">
                        <label>Subject</label>
                        <input type="text" id="e-subject" value="${escapeHtml(lesson.subject)}" required>
                    </div>
                    <div class="form-group">
                        <label>Description</label>
                        <input type="text" id="e-description" value="${escapeHtml(lesson.description || '')}">
                    </div>
                    <div class="form-group">
                        <label>Content</label>
                        <textarea id="e-content" rows="6" required>${escapeHtml(lesson.content || '')}</textarea>
                    </div>
                    <div class="form-group">
                        <label>Duration (minutes)</label>
                        <input type="number" id="e-duration" value="${lesson.durationMinutes}" min="1" required>
                    </div>
                    <div class="form-group">
                        <label>Access</label>
                        <select id="e-access">
                            <option value="FREE" ${lesson.accessType === 'FREE' ? 'selected' : ''}>Free</option>
                            <option value="PREMIUM" ${lesson.accessType === 'PREMIUM' ? 'selected' : ''}>Premium</option>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary w-full">Save Changes</button>
                </form>
            </div>
        `;
        document.getElementById('edit-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const body = {
                title: document.getElementById('e-title').value,
                subject: document.getElementById('e-subject').value,
                description: document.getElementById('e-description').value,
                content: document.getElementById('e-content').value,
                durationMinutes: parseInt(document.getElementById('e-duration').value, 10),
                accessType: document.getElementById('e-access').value
            };
            try {
                await put(`/api/lessons/${id}`, body);
                navigate('lesson', { id });
            } catch (err) {
                showAlert('edit-alert', err.message, 'danger');
            }
        });
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger max-w-md">${err.message}</div>`;
    }
}

async function renderSubscriptions(container) {
    if (!isAuth()) { navigate('login'); return; }
    container.innerHTML = `
        <div class="max-w-md">
            <h2 class="page-title">Subscriptions</h2>
            <div id="sub-alert"></div>
            <div id="sub-current"></div>
            <hr style="margin:1.5rem 0;border-color:var(--border)">
            <h3 class="mb-1">Subscribe</h3>
            <div class="card mb-2">
                <form id="sub-form">
                    <div class="form-group">
                        <label>Plan</label>
                        <select id="sub-plan" class="w-full">
                            <option value="BASIC">Basic — 1 month</option>
                            <option value="PRO">Pro — 1 month</option>
                            <option value="ANNUAL_PRO">Annual Pro — 1 year</option>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-success w-full">Subscribe</button>
                </form>
            </div>
            <h3 class="mb-1">History</h3>
            <div id="sub-history"></div>
        </div>
    `;

    try {
        const current = await get('/api/subscriptions/current');
        const curEl = document.getElementById('sub-current');
        if (current) {
            curEl.innerHTML = `
                <div class="card">
                    <div class="flex justify-between items-center mb-1">
                        <span class="font-bold">${current.plan}</span>
                        <span class="badge ${current.active ? 'badge-active' : 'badge-expired'}">${current.status}</span>
                    </div>
                    <div class="text-sm text-muted">Valid until ${current.expiryDate}</div>
                    ${current.active ? `<button class="btn btn-danger w-full mt-2" onclick="cancelSub()">Cancel</button>` : ''}
                </div>
            `;
        } else {
            curEl.innerHTML = `<div class="alert alert-info">You have no active subscription.</div>`;
        }
    } catch (err) {
        document.getElementById('sub-current').innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }

    try {
        const history = await get('/api/subscriptions/history');
        const histEl = document.getElementById('sub-history');
        if (!history || history.length === 0) {
            histEl.innerHTML = '<p class="text-muted">No subscription history.</p>';
        } else {
            histEl.innerHTML = history.map(h => `
                <div class="card mb-1" style="padding:0.75rem 1rem;">
                    <div class="flex justify-between items-center">
                        <span class="font-bold">${h.plan}</span>
                        <span class="badge ${h.status === 'ACTIVE' ? 'badge-active' : h.status === 'EXPIRED' ? 'badge-expired' : 'badge-cancelled'}">${h.status}</span>
                    </div>
                    <div class="text-sm text-muted">${h.startDate} → ${h.expiryDate}</div>
                </div>
            `).join('');
        }
    } catch (err) {
        document.getElementById('sub-history').innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }

    document.getElementById('sub-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const plan = document.getElementById('sub-plan').value;
        try {
            await post('/api/subscriptions', { plan });
            showAlert('sub-alert', 'Subscribed successfully!', 'success');
            renderSubscriptions(container);
        } catch (err) {
            showAlert('sub-alert', err.message, 'danger');
        }
    });
}

async function cancelSub() {
    if (!confirm('Cancel your active subscription?')) return;
    try {
        await del('/api/subscriptions/cancel');
        renderSubscriptions(document.getElementById('app'));
    } catch (err) {
        showAlert('sub-alert', err.message, 'danger');
    }
}

async function renderProfile(container) {
    if (!isAuth()) { navigate('login'); return; }
    container.innerHTML = `<div class="max-w-md"><div class="skeleton" style="height:150px"></div></div>`;
    try {
        const user = await get('/api/users/me');
        container.innerHTML = `
            <div class="max-w-md">
                <h2 class="page-title">Profile</h2>
                <div id="profile-alert"></div>
                <div class="card mb-2">
                    <div class="mb-1"><b>Name:</b> ${escapeHtml(user.name || '')}</div>
                    <div class="mb-1"><b>Email:</b> ${escapeHtml(user.email || '')}</div>
                    <div class="mb-1"><b>Role:</b> ${escapeHtml((user.roles || []).map(r => r.replace('ROLE_','')).join(', '))}</div>
                    <div class="mb-1"><b>Bio:</b> ${escapeHtml(user.bio || 'No bio set.')}</div>
                    <div class="text-sm text-muted">Joined ${user.createdAt ? new Date(user.createdAt).toLocaleDateString() : ''}</div>
                </div>
                <h3 class="mb-1">Update Profile</h3>
                <form id="profile-form" class="card">
                    <div class="form-group">
                        <label>Name</label>
                        <input type="text" id="p-name" value="${escapeHtml(user.name || '')}">
                    </div>
                    <div class="form-group">
                        <label>Bio</label>
                        <input type="text" id="p-bio" value="${escapeHtml(user.bio || '')}">
                    </div>
                    <div class="form-group">
                        <label>New Password</label>
                        <input type="password" id="p-password" placeholder="Leave blank to keep current">
                    </div>
                    <button type="submit" class="btn btn-primary w-full">Save</button>
                </form>
            </div>
        `;
        document.getElementById('profile-form').addEventListener('submit', async (e) => {
            e.preventDefault();
            const updates = {};
            const name = document.getElementById('p-name').value;
            const bio = document.getElementById('p-bio').value;
            const password = document.getElementById('p-password').value;
            if (name) updates.name = name;
            if (bio) updates.bio = bio;
            if (password) updates.password = password;
            try {
                const updated = await put('/api/users/me', updates);
                setAuth(state.token, { ...state.user, name: updated.name });
                showAlert('profile-alert', 'Profile updated.', 'success');
                renderProfile(container);
            } catch (err) {
                showAlert('profile-alert', err.message, 'danger');
            }
        });
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger max-w-md">${err.message}</div>`;
    }
}

async function renderTutors(container) {
    container.innerHTML = `<div class="skeleton" style="height:120px"></div>`;
    try {
        const tutors = await get('/api/users/tutors');
        container.innerHTML = `<h2 class="page-title">Tutors</h2>`;
        if (!tutors || tutors.length === 0) {
            container.innerHTML += '<p class="text-muted">No tutors found.</p>';
            return;
        }
        container.innerHTML += `<div class="card-grid">${tutors.map(t => `
            <div class="card">
                <div class="font-bold mb-1">${escapeHtml(t.name || '')}</div>
                <div class="text-sm text-muted">${escapeHtml(t.bio || 'No bio.')}</div>
                <button class="btn btn-secondary w-full mt-2" onclick="navigate('lessons'); searchByTutor('${t.id}')">View Lessons</button>
            </div>
        `).join('')}</div>`;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

async function searchByTutor(tutorId) {
    const app = document.getElementById('app');
    app.innerHTML = `
        <h2 class="page-title">Lessons by Tutor</h2>
        <div id="tutor-lessons"><div class="skeleton" style="height:120px"></div></div>
    `;
    try {
        const lessons = await get(`/api/lessons/tutor/${tutorId}`);
        const el = document.getElementById('tutor-lessons');
        el.innerHTML = lessons && lessons.length
            ? `<div class="card-grid">${lessons.map(l => lessonCard(l)).join('')}</div>`
            : '<p class="text-muted">No lessons from this tutor.</p>';
    } catch (err) {
        app.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

async function renderStudents(container) {
    if (!isTutor() && !isAdmin()) { navigate('lessons'); return; }
    container.innerHTML = `<div class="skeleton" style="height:120px"></div>`;
    try {
        const students = await get('/api/users/students');
        container.innerHTML = `<h2 class="page-title">Students</h2>`;
        if (!students || students.length === 0) {
            container.innerHTML += '<p class="text-muted">No students found.</p>';
            return;
        }
        container.innerHTML += `<div class="card-grid">${students.map(s => `
            <div class="card">
                <div class="font-bold mb-1">${escapeHtml(s.name || '')}</div>
                <div class="text-sm text-muted">${escapeHtml(s.email || '')}</div>
                <div class="text-sm text-muted">${escapeHtml(s.bio || 'No bio.')}</div>
            </div>
        `).join('')}</div>`;
    } catch (err) {
        container.innerHTML = `<div class="alert alert-danger">${err.message}</div>`;
    }
}

// ── Utilities ────────────────────────────────────────────
function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function showAlert(containerId, message, type = 'danger') {
    const html = `<div class="alert alert-${type}">${escapeHtml(message)}</div>`;
    if (containerId) {
        const el = document.getElementById(containerId);
        if (el) el.innerHTML = html;
    } else {
        const app = document.getElementById('app');
        if (app) app.insertAdjacentHTML('afterbegin', html);
    }
}

// ── Init ─────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    renderNav();
    render();
});
