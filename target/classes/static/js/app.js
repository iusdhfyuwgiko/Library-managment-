const state = {
    books: [],
    members: []
};

const elements = {
    booksTableBody: document.getElementById('booksTableBody'),
    statBooks: document.getElementById('statBooks'),
    statAvailable: document.getElementById('statAvailable'),
    statBorrowed: document.getElementById('statBorrowed'),
    statOverdue: document.getElementById('statOverdue'),
    statMembers: document.getElementById('statMembers'),
    statDueToday: document.getElementById('statDueToday'),
    statBooksMini: document.getElementById('statBooksMini'),
    statMembersMini: document.getElementById('statMembersMini'),
    statAvailableMini: document.getElementById('statAvailableMini'),
    statusBar: document.getElementById('statusBar'),
    todayDate: document.getElementById('todayDate'),
    greetingText: document.getElementById('greetingText'),
    librarySearch: document.getElementById('librarySearch'),
    searchFeedback: document.getElementById('searchFeedback'),
    activityList: document.getElementById('activityList'),
    activityEmpty: document.getElementById('activityEmpty'),
    popularBooks: document.getElementById('popularBooks'),
    memberList: document.getElementById('memberList'),
    membersPanelCount: document.getElementById('membersPanelCount'),
    membersEmpty: document.getElementById('membersEmpty'),
    categoryBreakdown: document.getElementById('categoryBreakdown'),
    categoryDonut: document.getElementById('categoryDonut'),
    categoryTotal: document.getElementById('categoryTotal'),
    categoryBars: document.getElementById('categoryBars'),
    recentLoansBody: document.getElementById('recentLoansBody'),
    recentLoansEmpty: document.getElementById('recentLoansEmpty'),
    overdueBooksBody: document.getElementById('overdueBooksBody'),
    overdueBooksEmpty: document.getElementById('overdueBooksEmpty'),
    addBookForm: document.getElementById('addBookForm'),
    addMemberForm: document.getElementById('addMemberForm'),
    issueBookForm: document.getElementById('issueBookForm'),
    returnBookForm: document.getElementById('returnBookForm'),
    themeToggle: document.getElementById('themeToggle'),
    sidebarToggle: document.querySelector('.sidebar-toggle')
};

/* ------------------------------------------------------------------ */
/* Theme                                                               */
/* ------------------------------------------------------------------ */

function setTheme(theme) {
    const isLight = theme === 'light';
    document.body.classList.toggle('theme-light', isLight);
    if (elements.themeToggle) {
        elements.themeToggle.textContent = isLight ? '🌙 Dark mode' : '☀️ Light mode';
    }
}

if (elements.themeToggle) {
    elements.themeToggle.addEventListener('click', function () {
        const isLight = document.body.classList.contains('theme-light');
        setTheme(isLight ? 'dark' : 'light');
    });
}

/* ------------------------------------------------------------------ */
/* Sidebar + navigation (no page reload)                               */
/* ------------------------------------------------------------------ */

// nav item  ->  id of the element on the page to scroll to
const NAV_TARGETS = {
    dashboard: 'dashboard',
    books: 'book-inventory',
    members: 'members-dashboard',
    borrowing: 'borrowing-panel',
    returns: 'returns-panel',
    reservations: 'reservations-panel',
    categories: 'category-insights',
    authors: 'book-inventory',
    fines: 'returns-panel',
    reports: 'circulation-trend',
    notifications: 'recent-activity',
    settings: 'themeToggle'
};

function setActiveNav(name) {
    document.querySelectorAll('[data-nav]').forEach((link) => {
        const isActive = link.dataset.nav === name;
        const label = link.querySelector('span:last-child')?.textContent.trim() || 'Navigate';
        link.classList.toggle('active', isActive);
        link.title = label;
        link.setAttribute('aria-label', label);
        if (isActive) {
            link.setAttribute('aria-current', 'page');
        } else {
            link.removeAttribute('aria-current');
        }
    });
}

function goToSection(name, { smooth = true, updateHash = true } = {}) {
    const targetId = NAV_TARGETS[name];
    const target = targetId ? document.getElementById(targetId) : null;
    if (!target) return;

    target.scrollIntoView({ behavior: smooth ? 'smooth' : 'auto', block: 'start' });
    setActiveNav(name);

    if (updateHash) {
        history.replaceState(null, '', `#${name}`);
    }

    // Settings points at the theme button, so give it focus for visibility
    if (name === 'settings') {
        elements.themeToggle?.focus({ preventScroll: true });
    }
}

// Highlight the sidebar item for whichever section is on screen while scrolling
function setupScrollSpy() {
    if (!('IntersectionObserver' in window)) return;

    // one nav name per unique target (first one wins when targets are shared)
    const targetToNav = {};
    Object.entries(NAV_TARGETS).forEach(([nav, id]) => {
        if (!targetToNav[id]) targetToNav[id] = nav;
    });

    const observer = new IntersectionObserver((entries) => {
        const visible = entries
            .filter((entry) => entry.isIntersecting)
            .sort((a, b) => b.intersectionRatio - a.intersectionRatio)[0];
        if (visible) {
            setActiveNav(targetToNav[visible.target.id]);
        }
    }, { rootMargin: '-20% 0px -60% 0px', threshold: [0, 0.25, 0.5, 1] });

    Object.keys(targetToNav).forEach((id) => {
        // skip the theme button, it is not a section
        if (id === 'themeToggle') return;
        const el = document.getElementById(id);
        if (el) observer.observe(el);
    });
}

function activateSection() {
    document.querySelectorAll('[data-nav]').forEach((link) => {
        link.addEventListener('click', (event) => {
            event.preventDefault();
            goToSection(link.dataset.nav);
        });
    });

    // Open directly on a section if the URL has #books, #members, etc.
    const initial = location.hash.replace('#', '') || document.body.dataset.activeSection || 'dashboard';
    setActiveNav(NAV_TARGETS[initial] ? initial : 'dashboard');
    if (NAV_TARGETS[initial] && initial !== 'dashboard') {
        requestAnimationFrame(() => goToSection(initial, { smooth: false, updateHash: false }));
    }

    setupScrollSpy();
}

if (elements.sidebarToggle) {
    elements.sidebarToggle.setAttribute('aria-expanded', 'true');
    elements.sidebarToggle.addEventListener('click', () => {
        const collapsed = document.body.classList.toggle('sidebar-collapsed');
        elements.sidebarToggle.setAttribute('aria-expanded', String(!collapsed));
        elements.sidebarToggle.setAttribute('aria-label', collapsed ? 'Expand sidebar' : 'Collapse sidebar');
        elements.sidebarToggle.title = collapsed ? 'Expand sidebar' : 'Collapse sidebar';
    });
}

/* ------------------------------------------------------------------ */
/* HTTP helpers                                                        */
/* ------------------------------------------------------------------ */

async function fetchJson(url, options = {}) {
    const response = await fetchWithCsrf(url, {
        ...options,
        headers: { 'Content-Type': 'application/json', ...options.headers }
    });

    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || 'Request failed');
    }

    return response.status === 204 ? null : response.json();
}

function fetchWithCsrf(url, options = {}) {
    const headers = new Headers(options.headers || {});
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    if (token && csrfHeader) {
        headers.set(csrfHeader, token);
    }

    return fetch(url, { ...options, headers });
}

function setStatus(message, isError = false) {
    if (!elements.statusBar) return;
    elements.statusBar.textContent = message;
    elements.statusBar.style.color = isError ? '#fca5a5' : '#d1fae5';
}

/* ------------------------------------------------------------------ */
/* Rendering                                                           */
/* ------------------------------------------------------------------ */

function renderBooks(books = state.books) {
    if (!elements.booksTableBody) return;

    elements.booksTableBody.innerHTML = '';
    books.forEach((book) => {
        const row = document.createElement('tr');
        const statusClass = book.availableCopies === 0 ? 'out' : book.availableCopies < 2 ? 'low' : 'available';
        const statusText = book.availableCopies === 0 ? 'Out of stock' : book.availableCopies < 2 ? 'Low stock' : 'Available';

        row.innerHTML = `
            <td>${book.bookId}</td>
            <td>${book.title}</td>
            <td>${book.author}</td>
            <td>${book.category}</td>
            <td>${book.availableCopies}</td>
            <td>${book.totalCopies}</td>
            <td><span class="badge ${statusClass}">${statusText}</span></td>
        `;
        elements.booksTableBody.appendChild(row);
    });

    if (!books.length) {
        const row = document.createElement('tr');
        row.className = 'table-empty-row';
        const cell = document.createElement('td');
        cell.colSpan = 7;
        cell.textContent = state.books.length ? 'No books match your search.' : 'No books in the catalog yet.';
        row.appendChild(cell);
        elements.booksTableBody.appendChild(row);
    }
}

function getLoans() {
    return state.books.flatMap((book) => (Array.isArray(book.loans) ? book.loans : []).map((loan) => ({
        ...loan,
        bookTitle: loan.book?.title || book.title,
        memberName: loan.member?.name || loan.member?.memberId || loan.memberId || 'Member'
    })));
}

function formatDate(value) {
    if (!value) return '—';
    const date = new Date(`${value}T00:00:00`);
    return Number.isNaN(date.getTime()) ? value : new Intl.DateTimeFormat('en', { day: '2-digit', month: 'short' }).format(date);
}

function renderActivityList() {
    if (!elements.activityList) return;

    const loans = getLoans().sort((a, b) => (b.returnDate || b.issueDate || '').localeCompare(a.returnDate || a.issueDate || '')).slice(0, 5);
    elements.activityList.innerHTML = loans.map((loan) => {
        const returned = Boolean(loan.returnDate);
        const date = returned ? loan.returnDate : loan.issueDate;
        return `
        <li class="activity-item">
            <span class="activity-icon ${returned ? 'returned' : 'issued'}">${returned ? '↩' : '✓'}</span>
            <div class="activity-copy">
                <strong>Book ${returned ? 'returned' : 'issued'}</strong>
                <span>${loan.bookTitle} · ${loan.memberName}</span>
            </div>
            <time>${formatDate(date)}</time>
        </li>
    `;
    }).join('');
    if (elements.activityEmpty) elements.activityEmpty.hidden = loans.length > 0;
}

function renderPopularBooks() {
    if (!elements.popularBooks) return;

    const books = [...state.books].sort((a, b) => (b.totalCopies - b.availableCopies) - (a.totalCopies - a.availableCopies));
    const topBooks = books.slice(0, 4);

    elements.popularBooks.innerHTML = topBooks.map((book, index) => `
        <div class="mini-book-card">
            <div class="mini-book-cover"></div>
            <div>
                <strong>${book.title}</strong>
                <small>${book.author}</small>
            </div>
            <span class="mini-book-score">#${index + 1}</span>
        </div>
    `).join('') || '<p class="empty-state">No books available yet.</p>';
}

function renderMembersList() {
    if (!elements.memberList) return;

    const members = state.members.slice(0, 8);
    if (elements.membersPanelCount) {
        elements.membersPanelCount.textContent = `${state.members.length} registered`;
    }
    if (elements.membersEmpty) elements.membersEmpty.hidden = members.length > 0;

    elements.memberList.innerHTML = members.map((member) => {
        const initials = String(member.name || 'Member')
            .split(/\s+/)
            .filter(Boolean)
            .slice(0, 2)
            .map((part) => part[0])
            .join('')
            .toUpperCase();
        const activeLoans = Array.isArray(member.loans)
            ? member.loans.filter((loan) => !loan.returnDate).length
            : 0;
        const loanLabel = `${activeLoans} active ${activeLoans === 1 ? 'loan' : 'loans'}`;

        return `
            <div class="member-row">
                <span class="member-avatar">${initials || 'M'}</span>
                <span class="member-details">
                    <strong>${member.name || 'Unnamed member'}</strong>
                    <small>${member.email || member.memberId || ''}</small>
                </span>
                <span class="member-meta">
                    <strong>${loanLabel}</strong>
                    <small>ID ${member.memberId || '—'}</small>
                </span>
            </div>
        `;
    }).join('');
}

function renderCategoryBreakdown() {
    if (!elements.categoryBreakdown) return;

    const categories = {};
    state.books.forEach((book) => {
        const key = book.category || 'General';
        categories[key] = (categories[key] || 0) + 1;
    });

    const sorted = Object.entries(categories).sort((a, b) => b[1] - a[1]);
    const topCategories = sorted.slice(0, 5);
    const otherCount = sorted.slice(5).reduce((sum, [, count]) => sum + count, 0);
    if (otherCount) topCategories.push(['Other', otherCount]);

    const palette = ['#4389F5', '#2CBF9B', '#F28C28', '#7781E8', '#E85A88', '#90A4B8'];
    const total = topCategories.reduce((sum, [, count]) => sum + count, 0);
    let start = 0;
    const stops = topCategories.map(([, count], index) => {
        const end = start + (total ? count / total * 100 : 0);
        const stop = `${palette[index % palette.length]} ${start}% ${end}%`;
        start = end;
        return stop;
    });

    if (elements.categoryDonut) {
        elements.categoryDonut.style.background = stops.length
            ? `conic-gradient(${stops.join(', ')})`
            : 'conic-gradient(var(--border) 0% 100%)';
    }
    if (elements.categoryTotal) elements.categoryTotal.textContent = state.books.length;

    elements.categoryBreakdown.innerHTML = topCategories.map(([name, count], index) => `
        <div class="legend-row">
            <span class="legend-dot" style="--legend-color:${palette[index % palette.length]}"></span>
            <span class="legend-name">${name}</span>
            <span class="legend-value">${count} <small>(${total ? Math.round(count / total * 100) : 0}%)</small></span>
        </div>
    `).join('') || '<p class="empty-state">Category data will appear when books are added.</p>';

    if (elements.categoryBars) {
        const max = Math.max(...topCategories.map(([, count]) => count), 1);
        elements.categoryBars.innerHTML = topCategories.map(([name, count], index) => `
            <div class="category-column">
                <span class="category-column-value">${count}</span>
                <span class="category-column-bar" style="--bar-height:${Math.max(5, count / max * 100)}%;--bar-color:${palette[index % palette.length]}"></span>
                <span class="category-column-label">${name}</span>
            </div>
        `).join('') || '<p class="empty-state">No category data yet.</p>';
    }
}

function renderLoanTables() {
    const loans = getLoans();
    const today = new Date().toISOString().slice(0, 10);
    const recent = [...loans].sort((a, b) => (b.issueDate || '').localeCompare(a.issueDate || '')).slice(0, 5);
    const overdue = loans.filter((loan) => !loan.returnDate && loan.dueDate && loan.dueDate < today)
        .sort((a, b) => a.dueDate.localeCompare(b.dueDate));

    if (elements.recentLoansBody) {
        elements.recentLoansBody.innerHTML = recent.map((loan) => `
            <tr>
                <td><strong>${loan.bookTitle || 'Book'}</strong></td>
                <td>${loan.memberName}</td>
                <td>${formatDate(loan.issueDate)}</td>
                <td>${formatDate(loan.dueDate)}</td>
            </tr>
        `).join('');
    }
    if (elements.recentLoansEmpty) elements.recentLoansEmpty.hidden = recent.length > 0;

    if (elements.overdueBooksBody) {
        elements.overdueBooksBody.innerHTML = overdue.map((loan) => {
            const days = Math.floor((new Date(`${today}T00:00:00`) - new Date(`${loan.dueDate}T00:00:00`)) / 86400000);
            return `
                <tr>
                    <td><strong>${loan.bookTitle || 'Book'}</strong></td>
                    <td>${loan.memberName}</td>
                    <td><span class="overdue-count">${days}</span></td>
                </tr>
            `;
        }).join('');
    }
    if (elements.overdueBooksEmpty) elements.overdueBooksEmpty.hidden = overdue.length > 0;

    return { loans, overdue, dueToday: loans.filter((loan) => !loan.returnDate && loan.dueDate === today).length };
}

function updateGreetingAndDate() {
    const now = new Date();
    const hour = now.getHours();
    if (elements.greetingText) {
        elements.greetingText.textContent = hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening';
    }
    if (elements.todayDate) {
        elements.todayDate.textContent = new Intl.DateTimeFormat('en', {
            weekday: 'short', day: '2-digit', month: 'short', year: 'numeric'
        }).format(now);
    }
}

function updateDashboard() {
    const totalBooks = state.books.length;
    const totalMembers = state.members.length;
    const totalAvailable = state.books.reduce((sum, book) => sum + Number(book.availableCopies || 0), 0);
    const totalBorrowed = state.books.reduce((sum, book) => sum + Math.max(0, (book.totalCopies || 0) - (book.availableCopies || 0)), 0);
    const loanStats = renderLoanTables();

    if (elements.statBooks) elements.statBooks.textContent = totalBooks;
    if (elements.statMembers) elements.statMembers.textContent = totalMembers;
    if (elements.statAvailable) elements.statAvailable.textContent = totalAvailable;
    if (elements.statBorrowed) elements.statBorrowed.textContent = totalBorrowed;
    if (elements.statOverdue) elements.statOverdue.textContent = loanStats.overdue.length;
    if (elements.statDueToday) elements.statDueToday.textContent = loanStats.dueToday;

    if (elements.statBooksMini) elements.statBooksMini.textContent = totalBooks;
    if (elements.statMembersMini) elements.statMembersMini.textContent = totalMembers;
    if (elements.statAvailableMini) elements.statAvailableMini.textContent = totalAvailable;

    renderActivityList();
    renderPopularBooks();
    renderMembersList();
    renderCategoryBreakdown();
    updateGreetingAndDate();
}

/* ------------------------------------------------------------------ */
/* Data loading                                                        */
/* ------------------------------------------------------------------ */

async function loadData() {
    try {
        const [booksResponse, membersResponse] = await Promise.all([
            fetch('/api/books'),
            fetch('/api/members')
        ]);

        if (!booksResponse.ok || !membersResponse.ok) {
            throw new Error('Server returned an error');
        }

        state.books = await booksResponse.json();
        state.members = await membersResponse.json();

        // keep an active search applied after reloading data
        const activeQuery = elements.librarySearch?.value.trim() || '';
        if (activeQuery) {
            await runSearch(activeQuery, { scroll: false });
        } else {
            renderBooks();
            if (elements.searchFeedback) elements.searchFeedback.textContent = `${state.books.length} books`;
        }

        updateDashboard();
        setStatus('Dashboard ready');
    } catch (error) {
        setStatus('Unable to load library data. Is the database running?', true);
    }
}

/* ------------------------------------------------------------------ */
/* Search                                                              */
/* ------------------------------------------------------------------ */

let searchTimer;
let searchWasActive = false;

function scrollToInventory() {
    document.getElementById('book-inventory')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    setActiveNav('books');
}

// Searches the server (/api/books/search) and shows matches in the inventory table
async function runSearch(query, { scroll = true } = {}) {
    let matches = state.books;

    if (query) {
        try {
            const response = await fetch(`/api/books/search?query=${encodeURIComponent(query)}`);
            if (!response.ok) throw new Error('Search failed');
            matches = await response.json();
        } catch (error) {
            // fall back to filtering the books already loaded in the browser
            const needle = query.toLocaleLowerCase();
            matches = state.books.filter((book) => [book.bookId, book.title, book.author, book.category]
                .some((value) => String(value || '').toLocaleLowerCase().includes(needle)));
        }
    }

    renderBooks(matches);

    if (elements.searchFeedback) {
        elements.searchFeedback.textContent = query
            ? `${matches.length} ${matches.length === 1 ? 'result' : 'results'}`
            : `${state.books.length} books`;
    }

    // Jump to the results the first time the user starts typing
    if (scroll && query && !searchWasActive) {
        scrollToInventory();
    }
    searchWasActive = Boolean(query);
}

if (elements.librarySearch) {
    elements.librarySearch.addEventListener('input', (event) => {
        clearTimeout(searchTimer);
        const query = event.target.value.trim();
        searchTimer = setTimeout(() => runSearch(query), 250);
    });

    elements.librarySearch.addEventListener('keydown', (event) => {
        if (event.key === 'Enter') {
            event.preventDefault();
            clearTimeout(searchTimer);
            runSearch(elements.librarySearch.value.trim(), { scroll: false });
            scrollToInventory();
        }
        if (event.key === 'Escape') {
            elements.librarySearch.value = '';
            runSearch('', { scroll: false });
        }
    });

    // Press "/" anywhere to focus the search bar
    document.addEventListener('keydown', (event) => {
        const tag = document.activeElement?.tagName;
        if (event.key === '/' && tag !== 'INPUT' && tag !== 'TEXTAREA') {
            event.preventDefault();
            elements.librarySearch.focus();
        }
    });
}

/* ------------------------------------------------------------------ */
/* Quick-action buttons                                                */
/* ------------------------------------------------------------------ */

document.querySelectorAll('[data-scroll-target]').forEach((button) => {
    button.addEventListener('click', () => {
        const target = document.getElementById(button.dataset.scrollTarget);
        target?.scrollIntoView({ behavior: 'smooth', block: 'start' });
        target?.querySelector('input')?.focus({ preventScroll: true });
    });
});

/* ------------------------------------------------------------------ */
/* Form handlers                                                       */
/* ------------------------------------------------------------------ */

async function handleAddBook(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const payload = {
        bookId: formData.get('bookId'),
        title: formData.get('title'),
        author: formData.get('author'),
        category: formData.get('category'),
        totalCopies: Number(formData.get('totalCopies')),
        availableCopies: Number(formData.get('totalCopies'))
    };

    try {
        await fetchJson('/api/books', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        event.target.reset();
        setStatus('Book added successfully.');
        await loadData();
    } catch (error) {
        setStatus(error.message || 'Failed to add book.', true);
    }
}

async function handleAddMember(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const payload = {
        memberId: formData.get('memberId'),
        name: formData.get('name'),
        email: formData.get('email')
    };

    try {
        await fetchJson('/api/members', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        event.target.reset();
        setStatus('Member added successfully.');
        await loadData();
    } catch (error) {
        setStatus(error.message || 'Failed to add member.', true);
    }
}

async function handleIssueBook(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const bookId = formData.get('bookId');
    const memberId = formData.get('memberId');

    try {
        const response = await fetchWithCsrf(`/api/books/${encodeURIComponent(bookId)}/issue?memberId=${encodeURIComponent(memberId)}`, {
            method: 'POST'
        });

        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || 'Issue failed');
        }

        const result = await response.json();
        const success = result.success === true || result.success === 'true';
        event.target.reset();
        setStatus(success ? 'Book issued successfully.' : 'Issue failed.');
        await loadData();
    } catch (error) {
        setStatus(error.message || 'Issue request failed.', true);
    }
}

async function handleReturnBook(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const bookId = formData.get('bookId');
    const returnDate = formData.get('returnDate') || '';
    const url = returnDate
        ? `/api/books/${encodeURIComponent(bookId)}/return?returnDate=${encodeURIComponent(returnDate)}`
        : `/api/books/${encodeURIComponent(bookId)}/return`;

    try {
        const response = await fetchWithCsrf(url, { method: 'POST' });
        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || 'Return failed');
        }

        const result = await response.json();
        const success = result.success === true || result.success === 'true';
        event.target.reset();
        setStatus(success ? 'Book returned successfully.' : 'Return failed.');
        await loadData();
    } catch (error) {
        setStatus(error.message || 'Return request failed.', true);
    }
}

if (elements.addBookForm) elements.addBookForm.addEventListener('submit', handleAddBook);
if (elements.addMemberForm) elements.addMemberForm.addEventListener('submit', handleAddMember);
if (elements.issueBookForm) elements.issueBookForm.addEventListener('submit', handleIssueBook);
if (elements.returnBookForm) elements.returnBookForm.addEventListener('submit', handleReturnBook);

/* ------------------------------------------------------------------ */
/* Init                                                                */
/* ------------------------------------------------------------------ */

setTheme('light');
activateSection();
loadData();