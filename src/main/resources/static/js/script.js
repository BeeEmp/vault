// Theme Logic
const themeBtn = document.querySelector('.theme-toggle');
const body = document.body;

function updateThemeBtnText(isDark) {
    if (themeBtn) {
        themeBtn.textContent = isDark ? "Light Mode" : "Dark Mode";
    }
}

function toggleTheme() {
    body.classList.toggle('dark-theme');
    const isDark = body.classList.contains('dark-theme');
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
    updateThemeBtnText(isDark);
}

if (localStorage.getItem('theme') === 'light') {
    body.classList.remove('dark-theme');
    updateThemeBtnText(false);
} else {
    // Default to Dark
    body.classList.add('dark-theme');
    updateThemeBtnText(true);
}

// --- Custom Select Logic ---
function setupCustomSelects() {
    const customSelects = document.querySelectorAll('.custom-select');

    customSelects.forEach(select => {
        const trigger = select.querySelector('.select-trigger');
        const options = select.querySelectorAll('.option');
        const hiddenInput = select.querySelector('input[type="hidden"]');

        trigger.addEventListener('click', () => {
            // Close others
            customSelects.forEach(s => {
                if (s !== select) s.classList.remove('open');
            });
            select.classList.toggle('open');
        });

        options.forEach(option => {
            option.addEventListener('click', () => {
                const value = option.getAttribute('data-value');
                const iconClass = option.getAttribute('data-icon'); // undefined for time select
                const text = option.innerText;

                // Update trigger
                if (iconClass) {
                    trigger.innerHTML = `<i class="${iconClass}"></i> <span>${text}</span>`;
                } else {
                    trigger.innerHTML = `<span>${text}</span>`;
                }

                // Update Value
                hiddenInput.value = value;

                select.classList.remove('open');
            });
        });
    });

    // Close when clicking outside
    document.addEventListener('click', (e) => {
        if (!e.target.closest('.custom-select')) {
            customSelects.forEach(s => s.classList.remove('open'));
        }
    });
}
// Init Custom Selects
document.addEventListener('DOMContentLoaded', setupCustomSelects);


// --- API Logic ---

// API Logic - Create
// --- Tabs Logic ---
function switchTab(tabId) {
    const createTab = document.getElementById('create-tab');
    const historyTab = document.getElementById('history-tab');
    const tabs = document.querySelectorAll('.tab');

    if (tabId === 'create') {
        createTab.style.display = 'block';
        historyTab.style.display = 'none';
        tabs[0].classList.add('active');
        tabs[1].classList.remove('active');
    } else {
        createTab.style.display = 'none';
        historyTab.style.display = 'block';
        tabs[0].classList.remove('active');
        tabs[1].classList.add('active');
        loadHistory();
    }
}

// --- History Logic ---
// Fetch and display the user's snippet history
async function loadHistory() {
    const historyList = document.getElementById('history-list');
    historyList.innerHTML = '<p style="padding: 20px; text-align: center;">Loading...</p>';

    try {
        // Fetch: Call backend API
        // cache: 'no-store' ensures we always get fresh data (fixes deletion lag)
        const response = await fetch('/api/snippets/history', { cache: 'no-store' });
        if (response.ok) {
            const snippets = await response.json();
            if (snippets.length === 0) {
                historyList.innerHTML = '<p style="padding: 20px; text-align: center; color: var(--text-muted);">No active snippets found.</p>';
            } else {
                // DOM Manipulation: Render list of snippets
                historyList.innerHTML = snippets.map(s => `
                    <div class="history-item" style="display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <h4 onclick="window.location.href='/view.html?id=${s.id}'">
                                ${s.title ? s.title : 'Untitled Snippet'} 
                                <span style="font-weight: normal; color: var(--text-muted); font-size: 0.8rem;">(${s.language})</span>
                            </h4>
                            <div class="history-meta">
                                Created: ${new Date(s.creationDate).toLocaleString()} &bull; Expires: ${new Date(s.expiryDate).toLocaleString()}
                            </div>
                        </div>
                        <button class="secondary delete-btn" data-id="${s.id}">
                            Delete
                        </button> // 'delete-btn' class used for Event Delegation
                    </div>
                `).join('');
            }
        } else {
            historyList.innerHTML = '<p style="color: red; text-align: center;">Failed to load history.</p>';
        }
    } catch (e) {
        historyList.innerHTML = '<p style="color: red; text-align: center;">Network error.</p>';
    }
}

// Event Delegation for History List
// Listener attached to parent ('history-list') to handle clicks on dynamic children ('delete-btn')
document.addEventListener('DOMContentLoaded', () => {
    const historyList = document.getElementById('history-list');
    if (historyList) {
        historyList.addEventListener('click', async (e) => {
            if (e.target && e.target.classList.contains('delete-btn')) {
                const id = e.target.getAttribute('data-id');
                if (id) {
                    await handleDelete(id);
                }
            }
        });
    }
});

// Async Function to handle deletion
async function handleDelete(id) {
    // Direct delete, no confirmation dialog
    try {
        const response = await fetch(`/api/snippets/${id}`, { method: 'DELETE' });
        if (response.ok) {
            loadHistory(); // Reload list to reflect changes
        } else {
            alert("Failed to delete. Server returned: " + response.status);
        }
    } catch (e) {
        console.error("Delete error:", e);
        alert("Network error.");
    }
}


// --- API Logic ---

// API Logic - Create
async function createSnippet() {
    const content = document.getElementById('code-content').value;
    const language = document.getElementById('language').value;
    const expiryTime = parseInt(document.getElementById('expiry-time').value);
    const title = document.getElementById('snippet-title').value; // New
    const btn = document.getElementById('create-btn');

    if (!content) return alert("Content cannot be empty");

    btn.disabled = true;
    btn.innerText = "Encrypting...";

    try {
        const response = await fetch('/api/snippets', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                content,
                language,
                title,
                expiryTime: expiryTime
            })
        });

        if (response.ok) {
            const data = await response.json();
            const link = `${window.location.origin}/view.html?id=${data.id}`;

            document.getElementById('success-message').style.display = 'block';
            document.getElementById('snippet-link').value = link;
            btn.innerText = "Created!";

            // Clear inputs logic optional
        } else {
            if (response.redirected) {
                window.location.href = response.url;
                return;
            }
            alert("Error creating snippet");
            btn.disabled = false;
            btn.innerText = "Create Secure Link";
        }
    } catch (e) {
        console.error(e);
        alert("Network error");
        btn.disabled = false;
        btn.innerText = "Create Secure Link";
    }
}


// API Logic - View
let currentSnippetData = null;

async function loadSnippet() {
    const params = new URLSearchParams(window.location.search);
    const id = params.get('id');

    if (!id) {
        showError();
        return;
    }

    try {
        const response = await fetch(`/api/snippets/${id}`);
        if (response.ok) {
            const data = await response.json();
            currentSnippetData = data;

            document.getElementById('loading').style.display = 'none';
            document.getElementById('content-area').style.display = 'block';

            // Show Header Download Button
            const viewDlBtn = document.getElementById('view-download-btn');
            if (viewDlBtn) viewDlBtn.style.display = 'block';

            document.getElementById('code-display').textContent = data.encryptedContent;

            // Map Icon Logic (Same as before)
            const iconMap = {
                // 'plaintext': Removed to handle manually
                'java': 'devicon-java-plain',
                'python': 'devicon-python-plain',
                'javascript': 'devicon-javascript-plain',
                'cplusplus': 'devicon-cplusplus-plain',
                'csharp': 'devicon-csharp-plain',
                'html': 'devicon-html5-plain'
            };
            const langNameMap = {
                'plaintext': 'Plain Text',
                'java': 'Java',
                'python': 'Python',
                'javascript': 'JavaScript',
                'cplusplus': 'C++',
                'csharp': 'C#',
                'html': 'HTML'
            };

            document.getElementById('language-badge').innerText = langNameMap[data.language] || data.language;

            // Update Title if exists, otherwise generic
            // Update Title if exists
            const filenameEl = document.getElementById('snippet-filename');
            if (data.title && filenameEl) {
                filenameEl.innerText = data.title;
                filenameEl.style.display = 'block';
                // Add a visual separator style if needed or rely on flex gap
                filenameEl.style.borderLeft = '1px solid var(--border-color)';
                filenameEl.style.paddingLeft = '12px';
                filenameEl.style.marginLeft = '4px';
            }

            // Set Icon
            const iconEl = document.getElementById('view-lang-icon');
            if (iconEl) {
                if (data.language === 'plaintext') {
                    // Replace icon class with text styling or inner HTML
                    iconEl.className = ''; // Remove icon classes
                    iconEl.style.fontFamily = 'monospace';
                    iconEl.style.fontWeight = 'bold';
                    iconEl.style.fontStyle = 'normal';
                    iconEl.innerText = '.txt';
                } else {
                    iconEl.className = iconMap[data.language] || 'devicon-codepen-plain';
                    iconEl.style.fontSize = '18px';
                    iconEl.innerText = ''; // Clear text if it was set
                }
            }

            document.getElementById('expiry-date').innerText = new Date(data.expiryDate).toLocaleString();
        } else {
            showError();
        }
    } catch (e) {
        showError();
    }
}

function showError() {
    document.getElementById('loading').style.display = 'none';
    document.getElementById('error-area').style.display = 'block';
}

function downloadSnippet() {
    if (!currentSnippetData) return;
    executeDownload(currentSnippetData.encryptedContent, currentSnippetData.language, currentSnippetData.title);
}

async function downloadRawSnippet() {
    const content = document.getElementById('code-content').value;
    const language = document.getElementById('language').value;
    const expiryTime = parseInt(document.getElementById('expiry-time').value);
    const title = document.getElementById('snippet-title').value;

    if (!content) return alert("Content is empty");

    // Save to history (create snippet) before downloading
    try {
        const response = await fetch('/api/snippets', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content, language, title, expiryTime })
        });

        if (response.ok) {
            // Refresh history if we are on that tab (optional, but good practice)
            loadHistory();
        }
    } catch (e) {
        console.error("Failed to save to history", e);
        // We continue to download even if save fails? Or alert? 
        // Let's continue so the user gets their file at least.
    }

    executeDownload(content, language, title);
}

function executeDownload(content, language, title) {
    const blob = new Blob([content], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');

    const extMap = {
        'java': '.java',
        'python': '.py',
        'javascript': '.js',
        'cplusplus': '.cpp',
        'csharp': '.cs',
        'html': '.html',
        'plaintext': '.txt'
    };
    const ext = extMap[language] || '.txt';
    let filename = title ? title : `snippet-${Date.now()}`;
    if (!filename.endsWith(ext)) filename += ext;

    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
}

async function copySnippetLink() {
    const linkInput = document.getElementById('snippet-link');
    const copyBtn = document.getElementById('copy-btn');

    if (!linkInput.value) return;

    try {
        await navigator.clipboard.writeText(linkInput.value);

        // Visual feedback
        const originalText = "Copy Link";
        copyBtn.innerText = "Copied!";

        setTimeout(() => {
            copyBtn.innerText = originalText;
        }, 2000);
    } catch (err) {
        console.error('Failed to copy: ', err);
        // Fallback
        linkInput.select();
        document.execCommand('copy');
        alert("Copied to clipboard!");
    }
}
