// Progressive enhancement: one GET form serves full pages, HTMX and both filter layouts.
(() => {
  document.documentElement.classList.add('catalog-js');
  const mobile = matchMedia('(max-width:700px)');
  const SEARCH_DELAY_MS = 300;
  const requests = new WeakMap();
  let searchTimer;
  let revision = 0;
  let activeRequest;
  let renderedQuery = document.querySelector('#catalog-search')?.value.trim() ?? '';
  let submitKind = 'search';
  const effectiveQuery = value => value.trim().length >= 3 ? value.trim() : '';
  function cancelSearch() {
    clearTimeout(searchTimer);
    searchTimer = undefined;
  }
  function invalidate() {
    revision++;
    if (activeRequest) {
      activeRequest.abort();
      activeRequest = undefined;
      document.querySelector('#catalog-state')?.removeAttribute('aria-busy');
    }
  }
  function submit(kind) {
    submitKind = kind;
    document.querySelector('#catalog-form')?.requestSubmit();
  }
  function scheduleSearch(input) {
    cancelSearch();
    invalidate();
    document.querySelector(".search-clear").hidden = !input.value;
    const query = effectiveQuery(input.value);
    if (query === renderedQuery) return;
    if (!input.value) submit('search');
    else searchTimer = setTimeout(() => submit('search'), SEARCH_DELAY_MS);
  }
  document.addEventListener('input', event => {
    if (event.target.id === 'catalog-search' && !event.isComposing) scheduleSearch(event.target);
  });
  document.addEventListener('compositionstart', event => {
    if (event.target.id === 'catalog-search') { cancelSearch(); invalidate(); }
  });
  document.addEventListener('compositionend', event => {
    if (event.target.id === 'catalog-search') scheduleSearch(event.target);
  });
  document.addEventListener('change', event => {
    if (event.target.id === 'catalog-sort') submit('sort');
  });
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
    if (event.target.closest('.search-clear')) {
      event.preventDefault();
      event.stopPropagation();
      const input = document.querySelector('#catalog-search');
      input.value = '';
      input.focus({preventScroll:true});
      submit('search');
      return;
    }
    if (event.target.closest('#catalog-state a[hx-get]')) { cancelSearch(); invalidate(); }

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
  }, true);
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && !mobile.matches) {
      const open = document.querySelector('.filter-group[open]');
      if (open) { open.open = false; open.querySelector('summary').focus(); event.preventDefault(); }
    }
  });
  document.addEventListener('submit', event => {
    if (event.target.id !== 'catalog-form') return;
    if (event.submitter?.matches('.apply-filter, .sheet-actions button')) submitKind = 'link';
    cancelSearch();
    invalidate();
    // Keep the native GET and the enhanced request valid when relevance disappears.
    const sort = event.target.querySelector('[name=sort]');
    if (!effectiveQuery(event.target.querySelector('[name=q]').value) && sort.value === 'relevance') sort.value = 'title-asc';
    const dialog = document.querySelector('#filters-dialog');
    if (dialog?.matches(':modal')) dialog.close();
  }, true);
  document.addEventListener('htmx:configRequest', event => {
    if (event.detail.elt.id !== 'catalog-form') return;
    event.detail.parameters.q = effectiveQuery(document.querySelector('#catalog-search').value);
  });
  document.addEventListener('htmx:beforeRequest', event => {
    if (event.detail.target?.id !== 'catalog-state') return;
    const formRequest = event.detail.elt.id === 'catalog-form';
    const kind = formRequest ? submitKind : 'link';
    submitKind = 'search';
    requests.set(event.detail.xhr, {
      revision,
      kind,
      formRequest,
      query: formRequest ? effectiveQuery(document.querySelector('#catalog-search').value) : null,
      expanded: Array.from(document.querySelectorAll('.theme-row[open]'), row => row.dataset.theme)
    });
    activeRequest = event.detail.xhr;
    document.querySelector('#catalog-state').setAttribute('aria-busy', 'true');
  });
  document.addEventListener('htmx:beforeSwap', event => {
    const request = requests.get(event.detail.xhr);
    if (!request) return;
    if (request.revision !== revision) { event.detail.shouldSwap = false; return; }
    const input = document.querySelector('#catalog-search');
    request.rawQuery = input.value;
    const focused = document.activeElement;
    // Snapshot focus at response time: do not pull users back after they tab away.
    if (request.kind !== 'link' && ['catalog-search', 'catalog-sort'].includes(focused?.id)) {
      request.focus = focused.id;
      request.start = focused.selectionStart;
      request.end = focused.selectionEnd;
      request.direction = focused.selectionDirection;
    }
  });
  document.addEventListener('htmx:afterSwap', event => {
    init();
    if (event.detail.target?.id !== 'catalog-state') return;
    const request = requests.get(event.detail.xhr);
    if (!request || request.revision !== revision) return;
    activeRequest = undefined;
    htmx.process(document.querySelector('#catalog-state'));
    document.querySelectorAll('.theme-row').forEach(row => { if (request.expanded.includes(row.dataset.theme)) row.open = true; });
    renderedQuery = request.query ?? document.querySelector('#catalog-search').value.trim();
    if (request.formRequest) document.querySelector('#catalog-search').value = request.rawQuery;
    document.querySelector('.search-clear').hidden = !document.querySelector('#catalog-search').value;
    if (request.focus) {
      const control = document.getElementById(request.focus);
      control.focus({preventScroll:true});
      if (request.focus === 'catalog-search' && request.start !== null)
        control.setSelectionRange(request.start, request.end, request.direction);
    } else if (request.kind === 'link') document.querySelector('#results-heading')?.focus({preventScroll:true});
  });
  document.addEventListener('htmx:historyRestore', () => {
    cancelSearch(); invalidate();
    renderedQuery = document.querySelector('#catalog-search')?.value.trim() ?? '';
    document.querySelector('#catalog-state')?.removeAttribute('aria-busy');
    init();
  });
  function failed(event) {
    const request = requests.get(event.detail.xhr);
    if (request && request.revision !== revision) return;
    activeRequest = undefined;
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
