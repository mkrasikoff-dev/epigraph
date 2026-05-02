/**
 * tags.js — Tag management for add-quote form and edit modal in Epigraph.
 *
 * Depends on:
 *   - escHtml() {fn} — defined in index.html UTILITIES
 *   - t()       {fn} — defined in i18n.js
 *
 * Exposes global state:
 *   - currentTags {Array}  — pending tags for the add-quote form
 *   - editTags    {Array}  — pending tags for the currently open edit modal
 */

// =============================================================================
// TAGS — ADD FORM
// =============================================================================

/** Pending tags for the add-quote form. */
let currentTags = [];

/**
 * Commits the active inline tag input: trims value, adds tag if non-empty,
 * then switches back to the "+" button state.
 * @param {HTMLInputElement} input
 */
function commitTagInput(input) {
    if (!input) return;
    const val = input.value.trim();
    if (val && !currentTags.includes(val)) {
        currentTags.push(val);
    }
    input.removeEventListener('blur', input._blurHandler);
    renderTags();
}

/**
 * Shows the inline tag input inside the add-quote form.
 */
function showTagInput() {
    renderTags(true);
    const wrap = document.getElementById('tags-wrap');
    const input = wrap?.querySelector('.tag-input');
    if (input) input.focus();
}

/**
 * Removes a tag from the add-form pending list and re-renders.
 * @param {string} tag - Tag value to remove.
 */
function removeTag(tag) {
    currentTags = currentTags.filter(existing => existing !== tag);
    renderTags();
}

/**
 * Re-renders the tag chips + add-button (or inline input) inside the add-quote form.
 * @param {boolean} [showInput=false] - If true, renders the inline input instead of the + button.
 */
function renderTags(showInput) {
    const wrap = document.getElementById('tags-wrap');
    if (!wrap) return;
    wrap.innerHTML = '';

    currentTags.forEach(tag => {
        const chip = document.createElement('span');
        chip.className = 'tag';
        chip.innerHTML = `${escHtml(tag)}<button type="button" onclick="removeTag('${escHtml(tag)}')" aria-label="${t('ariaTagRemove')}"><svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>`;
        wrap.appendChild(chip);
    });

    if (showInput) {
        const input = document.createElement('input');
        input.type = 'text';
        input.className = 'form-input tag-input';
        input.placeholder = t('placeholderTagInput');
        input.maxLength = 50;
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                input.removeEventListener('blur', input._blurHandler);
                commitTagInput(input);
            } else if (e.key === 'Escape') {
                input.removeEventListener('blur', input._blurHandler);
                renderTags();
            }
        });
        input._blurHandler = () => commitTagInput(input);
        input.addEventListener('blur', input._blurHandler);
        wrap.appendChild(input);
    } else {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'tag-add-btn';
        btn.setAttribute('aria-label', t('ariaTagAdd'));
        btn.title = t('ariaTagAdd');
        btn.innerHTML = `<svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg> ${t('tagAddButtonLabel')}`;
        btn.addEventListener('click', () => showTagInput());
        wrap.appendChild(btn);
    }
}

// =============================================================================
// TAGS — EDIT MODAL
// =============================================================================

/** Mutable tag list for the currently open edit modal. */
let editTags = [];

/**
 * Removes a tag from the edit modal pending list and re-renders.
 * @param {string} tag - Tag value to remove.
 */
function removeEditTag(tag) {
    editTags = editTags.filter(existing => existing !== tag);
    renderEditTags();
}

/**
 * Re-renders the tag chips + add-button (or inline input) inside the edit modal.
 * @param {boolean} [showInput=false] - If true, renders the inline input instead of the + button.
 */
function renderEditTags(showInput) {
    const wrap = document.getElementById('edit-tags-wrap');
    if (!wrap) return;
    wrap.innerHTML = '';

    editTags.forEach(tag => {
        const chip = document.createElement('span');
        chip.className = 'tag';
        chip.innerHTML = `${escHtml(tag)}<button type="button" onclick="removeEditTag('${escHtml(tag)}')" aria-label="${t('ariaTagRemove')}"><svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg></button>`;
        wrap.appendChild(chip);
    });

    if (showInput) {
        const input = document.createElement('input');
        input.type = 'text';
        input.className = 'form-input tag-input';
        input.placeholder = t('placeholderTagInput');
        input.maxLength = 50;
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                input.removeEventListener('blur', input._blurHandler);
                const val = input.value.trim();
                if (val && !editTags.includes(val)) editTags.push(val);
                renderEditTags();
            } else if (e.key === 'Escape') {
                input.removeEventListener('blur', input._blurHandler);
                renderEditTags();
            }
        });
        input._blurHandler = () => {
            const val = input.value.trim();
            if (val && !editTags.includes(val)) editTags.push(val);
            renderEditTags();
        };
        input.addEventListener('blur', input._blurHandler);
        wrap.appendChild(input);
        input.focus();
    } else {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'tag-add-btn';
        btn.setAttribute('aria-label', t('ariaTagAdd'));
        btn.title = t('ariaTagAdd');
        btn.innerHTML = `<svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg> ${t('tagAddButtonLabel')}`;
        btn.addEventListener('click', () => renderEditTags(true));
        wrap.appendChild(btn);
    }
}
