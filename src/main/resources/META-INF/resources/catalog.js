// Progressive enhancement: one GET form serves full pages, HTMX and both filter layouts.
(() => {
  document.documentElement.classList.add('catalog-js');
  const mobile = matchMedia('(max-width:700px)');
  let expanded = [];
  const enhanced = new WeakSet();
  function init() {
    document.querySelectorAll('.theme-row, .filter-group').forEach(details => {
      const sync = () => details.querySelector('summary').setAttribute('aria-expanded', String(details.open));
      sync();
      if (!enhanced.has(details)) {
        enhanced.add(details);
        details.addEventListener('toggle', () => {
          sync();
          if (details.open && details.matches('.filter-group') && !mobile.matches)
            document.querySelectorAll('.filter-group').forEach(other => { if (other !== details) other.open = false; });
        });
      }
    });
    const dialog = document.querySelector('#filters-dialog');
    if (dialog) {
      if (mobile.matches && !dialog.matches(':modal')) dialog.removeAttribute('open');
      if (!mobile.matches) { if (dialog.matches(':modal')) dialog.close(); dialog.setAttribute('open', ''); }
      document.querySelector('.mobile-filter-trigger')?.setAttribute('aria-expanded', String(dialog.matches(':modal')));
      if (!enhanced.has(dialog)) {
        enhanced.add(dialog);
        dialog.addEventListener('close', () => {
          document.querySelector('.mobile-filter-trigger')?.setAttribute('aria-expanded', 'false');
        });
      }
    }
  }
  document.addEventListener('click', event => {
    if (event.target.closest('.mobile-filter-trigger')) {
      const dialog = document.querySelector('#filters-dialog');
      dialog.showModal();
      dialog.querySelectorAll('.filter-group').forEach(group => { group.open = true; });
      event.target.closest('button').setAttribute('aria-expanded', 'true');
      dialog.querySelector('.sheet-close').focus();
    }
    if (event.target.closest('.sheet-close')) document.querySelector('#filters-dialog').close();
    if (!event.target.closest('.filter-group') && !mobile.matches)
      document.querySelectorAll('.filter-group[open]').forEach(group => { group.open = false; });
  });
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && !mobile.matches) {
      const open = document.querySelector('.filter-group[open]');
      if (open) { open.open = false; open.querySelector('summary').focus(); event.preventDefault(); }
    }
  });
  document.addEventListener('submit', event => {
    if (event.target.id !== 'catalog-form') return;
    // Clearing a search with relevance selected must remain a valid GET request, also with Enter.
    const sort = event.target.querySelector('[name=sort]');
    if (!event.target.querySelector('[name=q]').value.trim() && sort.value === 'relevance') sort.value = 'title-asc';
    const dialog = document.querySelector('#filters-dialog');
    if (dialog?.matches(':modal')) dialog.close();
  }, true);
  document.addEventListener('htmx:beforeRequest', event => {
    if (event.detail.target?.id === 'catalog-state') {
      expanded = Array.from(document.querySelectorAll('.theme-row[open]'), row => row.dataset.theme);
      document.querySelector('#catalog-state').setAttribute('aria-busy', 'true');
    }
  });
  document.addEventListener('htmx:afterSwap', event => {
    init();
    if (event.detail.target?.id === 'catalog-state') {
      // Process new disclosure listeners before restoring open state.
      htmx.process(document.querySelector('#catalog-state'));
      document.querySelectorAll('.theme-row').forEach(row => { if (expanded.includes(row.dataset.theme)) row.open = true; });
      document.querySelector('#results-heading')?.focus({preventScroll:true});
    }
  });
  document.addEventListener('htmx:historyRestore', () => {
    document.querySelector('#catalog-state')?.removeAttribute('aria-busy');
    init();
  });
  function failed(event) {
    const details = event.detail.elt?.closest('.theme-row');
    if (details) {
      let message = details.querySelector('.load-error');
      if (!message) {
        message = document.createElement('p'); message.className = 'load-error'; message.setAttribute('role', 'alert');
        details.querySelector('.theme-layers').append(message);
      }
      message.textContent = 'Die Kartenebenen konnten nicht geladen werden. Nutzen Sie den Themenlink.';
    } else if (event.detail.target?.id === 'catalog-state') {
      const state = document.querySelector('#catalog-state'); state.removeAttribute('aria-busy');
      const message = state.querySelector('.catalog-error'); message.hidden = false;
      message.textContent = 'Die Auswahl konnte nicht geladen werden. Bitte erneut versuchen oder die Seite neu laden.';
    }
  }
  document.addEventListener('htmx:responseError', failed);
  document.addEventListener('htmx:sendError', failed);
  mobile.addEventListener('change', init);
  init();
})();
