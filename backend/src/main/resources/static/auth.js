/**
 * auth.js — Authentication state and UI for Epigraph.
 *
 * Depends on:
 * - GUEST_QUOTES     {Array}    — preset quotes for guest mode, defined in index.html CONSTANTS
 * - quotes           {Array}    — global mutable quotes array, defined in index.html CONSTANTS
 * - currentQodIndex  {number}   — defined in index.html CONSTANTS
 * - loadData()       {fn}       — defined in api.js
 * - renderQod()      {fn}       — defined in quotes.js
 * - switchView()     {fn}       — defined in index.html NAVIGATION
 * - t()              {fn}       — defined in i18n.js
 * - AUTH_API         {string}   — defined in index.html CONSTANTS
 */

// =============================================================================
// AUTH STATE
// JWT token stored in localStorage, helpers to read/write/clear it.
// =============================================================================
function getToken() {
    try {
        return localStorage.getItem('epigraph_token');
    } catch (e) {
        return null;
    }
}

function setToken(token) {
    try {
        localStorage.setItem('epigraph_token', token);
    } catch (e) {
    }
}

function clearToken() {
    try {
        localStorage.removeItem('epigraph_token');
    } catch (e) {
    }
}

function authHeaders() {
    const token = getToken();
    return token
        ? {'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token}
        : {'Content-Type': 'application/json'};
}

// =============================================================================
// AUTH UI
// The block responsible for authentication screens, modes, and session flow.
// =============================================================================
let authMode = 'login';
let isGuest = true;

function showAuthModal() {
    document.getElementById('auth-screen').classList.add('visible');
    document.getElementById('app-blur-overlay').classList.add('visible');
}

function hideAuthModal() {
    document.getElementById('auth-screen').classList.remove('visible');
    document.getElementById('app-blur-overlay').classList.remove('visible');
}

function hideLoadingOverlay() {
    document.getElementById('app-loading-overlay')?.classList.add('hidden');
}

function showGuestMode() {
    isGuest = true;
    quotes = GUEST_QUOTES;

    document.getElementById('logout-btn').style.display = 'none';
    document.getElementById('login-btn').style.display = '';

    ['list', 'add', 'settings'].forEach(id => {
        document.getElementById('tab-' + id)?.classList.add('guest-locked');
        document.getElementById('btn-fav-qod')?.classList.add('guest-locked');
    });

    const accountGroup = document.getElementById('settings-account-group');

    if (accountGroup) accountGroup.style.display = 'none';

    renderQod();
    hideLoadingOverlay();
}

function hideGuestMode() {
    isGuest = false;

    document.getElementById('logout-btn').style.display = '';
    document.getElementById('login-btn').style.display = 'none';

    ['list', 'add', 'settings'].forEach(id => {
        document.getElementById('tab-' + id)?.classList.remove('guest-locked');
        document.getElementById('btn-fav-qod')?.classList.remove('guest-locked');
    });
}

/**
 * Updates the account section in Settings with current user's quote stats.
 * Shows the section for authenticated users, hides for guests.
 */
function updateSettingsAccount() {
    const accountGroup = document.getElementById('settings-account-group');
    if (!accountGroup) return;

    if (isGuest) {
        accountGroup.style.display = 'none';
        return;
    }

    accountGroup.style.display = '';

    const total = quotes.length;
    const favCount = quotes.filter(q => q.fav).length;

    document.getElementById('settings-account-stats').textContent =
        t('statsSummary', {total, word: pluralQuotes(total), favorites: favCount});
}

/**
 * Fetches the latest release tag from GitHub and displays it as the app version.
 * Caches the result in sessionStorage to avoid redundant API calls within the same session.
 */
async function loadAppVersion() {
    const badge = document.getElementById('settings-version-badge');

    if (!badge) return;

    const cached = sessionStorage.getItem('epigraph_version');

    if (cached) {
        badge.textContent = cached;
        return;
    }

    try {
        const response = await fetch('https://api.github.com/repos/mkrasikoff/epigraph/releases/latest');

        if (!response.ok) throw new Error('Failed to fetch release');

        const data = await response.json();
        const version = data.tag_name || 'v—';
        sessionStorage.setItem('epigraph_version', version);
        badge.textContent = version;
    } catch {
        badge.textContent = 'v—';
    }
}

// =============================================================================
// QUOTE OF THE DAY LOADER
// Fetches QoD from the backend and caches the result in sessionStorage
// to avoid redundant API calls within the same session.
// =============================================================================
/**
 * Loads the Quote of the Day from the backend, using sessionStorage cache
 * to avoid redundant requests within the same browser session.
 * Falls back to renderQod(null) if the request fails.
 */
async function loadQod() {
    const CACHE_KEY = 'epigraph_qod_id';

    try {
        const cachedId = sessionStorage.getItem(CACHE_KEY);
        if (cachedId !== null) {
            const quote = quotes.find(q => q.id === Number(cachedId)) || null;
            renderQod(quote);
            return;
        }
    } catch { /* corrupted cache — proceed to fetch */ }

    try {
        const qodQuote = await Api.getQod();
        if (qodQuote) {
            sessionStorage.setItem(CACHE_KEY, String(qodQuote.id));
        }
        renderQod(qodQuote);
    } catch {
        renderQod(null);
    }
}

// =============================================================================
// HASH ROUTING
// Syncs browser URL hash with the active view and handles back/forward navigation.
// Supported hashes: #today, #all, #add, #settings
// =============================================================================
/** Maps URL hashes to view identifiers. */
const HASH_TO_VIEW = {
    '#today':    'qod',
    '#all':      'list',
    '#add':      'add',
    '#settings': 'settings',
};

/**
 * Returns the view id for the current window.location.hash,
 * falling back to 'qod' for unknown or empty hashes.
 * @returns {string}
 */
function getViewFromHash() {
    return HASH_TO_VIEW[window.location.hash] || 'qod';
}

/**
 * Navigates to the view matching the current URL hash.
 * Called on hashchange (back/forward) and on initial load.
 */
function applyHashRoute() {
    switchView(getViewFromHash());
}

// Handle browser back / forward buttons
window.addEventListener('hashchange', applyHashRoute);

/**
 * Returns the correct Russian plural form for the word "цитата".
 * @param {number} n
 * @returns {string}
 */
function pluralQuotes(n) {
    const mod10 = n % 10;
    const mod100 = n % 100;

    if (mod10 === 1 && mod100 !== 11) return t('pluralQuote1');
    if (mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)) return t('pluralQuote2');

    return t('pluralQuote5');
}

function toggleAuthMode() {
    authMode = authMode === 'login' ? 'register' : 'login';
    const isRegister = authMode === 'register';

    document.getElementById('auth-submit-btn').textContent = isRegister ? t('authSubmitRegister') : t('authSubmitLogin');
    document.getElementById('auth-switch-text').textContent = isRegister ? t('authSwitchToLogin') : t('authSwitchToRegister');
    document.querySelector('#auth-screen .auth-switch button').textContent = isRegister ? t('authSwitchBtnLogin') : t('authSwitchBtnRegister');
    document.getElementById('auth-error').textContent = '';

    const card = document.querySelector('.auth-card');
    card.classList.toggle('auth-card--register', isRegister);

    const loginHeader = document.getElementById('auth-login-header');
    const registerPanel = document.getElementById('auth-register-panel');
    const registerFormCol = document.getElementById('auth-register-form-col');
    const subtitle = document.getElementById('auth-subtitle');

    if (loginHeader) loginHeader.style.display = isRegister ? 'none' : '';
    if (registerPanel) registerPanel.style.display = isRegister ? '' : 'none';
    if (registerFormCol) registerFormCol.style.display = isRegister ? '' : 'none';
    if (subtitle) subtitle.style.display = isRegister ? 'none' : '';

    if (!isRegister) {
        const regEmail = document.getElementById('auth-email-reg')?.value.trim();
        if (regEmail) document.getElementById('auth-email').value = regEmail;
    }

    const hint = document.getElementById('auth-password-hint');
    const passInput = document.getElementById('auth-password');

    if (hint) hint.style.display = isRegister ? 'block' : 'none';

    if (passInput) {
        passInput.placeholder = isRegister ? t('authPlaceholderPasswordRegister') : t('authPlaceholderPasswordLogin');
        passInput.autocomplete = isRegister ? 'new-password' : 'current-password';
    }
}

async function authSubmit() {
    const email = document.getElementById('auth-email').value.trim();
    const password = document.getElementById('auth-password').value;
    const errorEl = document.getElementById('auth-error');
    const btn = document.getElementById('auth-submit-btn');
    errorEl.textContent = '';

    if (!email || !password) {
        errorEl.textContent = t('authErrorFillAllFields');
        return;
    }

    const emailRegex = /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/;
    if (!emailRegex.test(email)) {
        errorEl.textContent = t('authErrorInvalidEmail');
        return;
    }

    if (authMode === 'register') {
        if (password.length < 8) {
            errorEl.textContent = t('authErrorPasswordTooShort');
            return;
        }
        if (password.length > 128) {
            errorEl.textContent = t('authErrorPasswordTooLong');
            return;
        }
        if (!/[A-Za-z]/.test(password)) {
            errorEl.textContent = t('authErrorPasswordNoLetter');
            return;
        }
        if (!/[0-9]/.test(password)) {
            errorEl.textContent = t('authErrorPasswordNoDigit');
            return;
        }
    }

    btn.disabled = true;
    btn.innerHTML = `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="animation:spin 0.7s linear infinite;flex-shrink:0"><path d="M21 12a9 9 0 1 1-6.219-8.56"/></svg> ${t('authLoading')}`;

    const endpoint = authMode === 'login' ? '/login' : '/register';

    try {
        const res = await fetch(AUTH_API + endpoint, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({email, password})
        });

        const data = await res.json();

        if (!res.ok) {
            if (data.password) {
                errorEl.textContent = data.password;
                return;
            }

            if (data.email) {
                errorEl.textContent = t('authErrorInvalidEmailServer');
                return;
            }

            errorEl.textContent = data.message || t('authErrorWrongCredentials');

            return;
        }

        setToken(data.token);
        hideAuthModal();
        hideGuestMode();
        await loadData();
        renderQod();

    } catch (e) {
        errorEl.textContent = t('authErrorConnection');
    } finally {
        btn.disabled = false;
        btn.textContent = authMode === 'register' ? t('authSubmitRegister') : t('authSubmitLogin');
    }
}

/**
 * Handles registration form submission from the two-column register layout.
 */
async function authSubmitRegister() {
    const email = document.getElementById('auth-email-reg')?.value.trim() || '';
    const password = document.getElementById('auth-password-reg')?.value || '';
    const errorEl = document.getElementById('auth-error-reg');

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
    const passwordRegex = /^(?=.*[A-Za-zА-Яа-яЁё])(?=.*\d).{8,}$/;

    if (!emailRegex.test(email)) {
        if (errorEl) errorEl.textContent = t('authErrorInvalidEmailDot');
        return;
    }

    if (!passwordRegex.test(password)) {
        if (errorEl) errorEl.textContent = t('authErrorPasswordPattern');
        return;
    }

    if (errorEl) errorEl.textContent = '';

    document.getElementById('auth-email').value = email;
    document.getElementById('auth-password').value = password;
    authMode = 'register';

    // Call /register — on success (202) backend sent a code, show verification screen
    const btn = document.getElementById('auth-submit-reg-btn');
    if (btn) { btn.disabled = true; btn.textContent = t('authLoading'); }

    try {
        const res = await fetch(AUTH_API + '/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const data = res.status === 202 ? null : await res.json().catch(() => null);

        if (!res.ok) {
            if (errorEl) errorEl.textContent = data?.message || t('authErrorWrongCredentials');
            return;
        }

        // Show the verification code input screen
        showVerifyScreen(email);

    } catch {
        if (errorEl) errorEl.textContent = t('authErrorConnection');
    } finally {
        if (btn) { btn.disabled = false; btn.textContent = t('authSubmitRegister'); }
    }
}

function loginWithGoogle() {
    window.location.href = '/oauth2/authorization/google';
}

function logout() {
    clearToken();
    showGuestMode();
    switchView('qod');
}

function handleAuthOverlayClick(e) {
    if (e.target === document.getElementById('auth-screen')) hideAuthModal();
}

/**
 * Shows the email verification screen after successful registration request.
 * @param {string} email - The email address the code was sent to.
 */
function showVerifyScreen(email) {
    // Reuse the auth modal — replace content with verification form
    const container = document.getElementById('auth-register-form-col');
    if (!container) return;

    container.innerHTML = `
        <h2 class="auth-title">${t('verifyTitle')}</h2>
        <p class="auth-subtitle">${t('verifySubtitle', { email })}</p>
        <div class="auth-field">
            <label class="auth-label">${t('verifyCodeLabel')}</label>
            <input id="verify-code-input" class="auth-input" type="text"
                   inputmode="numeric" maxlength="6" placeholder="000000"
                   autocomplete="one-time-code">
        </div>
        <p id="verify-error" class="auth-error"></p>
        <button id="verify-submit-btn" class="auth-btn-primary" onclick="submitVerifyCode('${email}')">
            ${t('verifySubmit')}
        </button>
        <p class="auth-hint">${t('verifyResendHint')} <a href="#" onclick="resendVerifyCode('${email}'); return false;">${t('verifyResendLink')}</a></p>
    `;

    // Auto-focus the code input
    setTimeout(() => document.getElementById('verify-code-input')?.focus(), 100);
}

/**
 * Submits the verification code to /api/auth/verify.
 * On success, logs the user in and closes the auth modal.
 * @param {string} email
 */
async function submitVerifyCode(email) {
    const codeInput = document.getElementById('verify-code-input');
    const errorEl = document.getElementById('verify-error');
    const btn = document.getElementById('verify-submit-btn');
    const code = codeInput?.value.trim();

    if (!code || code.length !== 6) {
        if (errorEl) errorEl.textContent = t('verifyErrorInvalidCode');
        return;
    }

    if (errorEl) errorEl.textContent = '';
    if (btn) { btn.disabled = true; btn.textContent = t('authLoading'); }

    try {
        const res = await fetch(AUTH_API + '/verify', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, code })
        });

        const data = await res.json().catch(() => null);

        if (!res.ok) {
            if (errorEl) errorEl.textContent = data?.message || t('verifyErrorInvalidCode');
            return;
        }

        // Registration complete — log the user in
        setToken(data.token);
        hideAuthModal();
        hideGuestMode();
        await loadData();
        renderQod();

    } catch {
        if (errorEl) errorEl.textContent = t('authErrorConnection');
    } finally {
        if (btn) { btn.disabled = false; btn.textContent = t('verifySubmit'); }
    }
}

/**
 * Re-sends the verification code for the given email.
 * @param {string} email
 */
async function resendVerifyCode(email) {
    const errorEl = document.getElementById('verify-error');
    try {
        const res = await fetch(AUTH_API + '/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email,
                password: document.getElementById('auth-password')?.value || '__resend__'
            })
        });
        if (errorEl) {
            errorEl.style.color = res.ok ? 'var(--color-accent)' : '';
            errorEl.textContent = res.ok ? t('verifyResendSuccess') : t('verifyResendError');
        }
    } catch {
        if (errorEl) errorEl.textContent = t('authErrorConnection');
    }
}
