// Progressive enhancement for native disclosures; normal theme links remain without JavaScript.
document.querySelectorAll('.theme-row').forEach(details => {
  details.querySelector('summary').setAttribute('aria-expanded', String(details.open));
  details.addEventListener('toggle', () => details.querySelector('summary').setAttribute('aria-expanded', String(details.open)));
});
document.addEventListener('htmx:responseError', event => {
  const details = event.detail.elt.closest('.theme-row');
  if (!details) return;
  let message = details.querySelector('.load-error');
  if (!message) {
    message = document.createElement('p');
    message.className = 'load-error';
    message.setAttribute('role', 'alert');
    details.querySelector('.theme-layers').append(message);
  }
  message.textContent = 'Die Kartenebenen konnten nicht geladen werden. Nutzen Sie den Themenlink.';
});
