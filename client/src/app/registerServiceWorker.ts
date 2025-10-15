/* eslint-env browser */
/* global navigator */

export function registerServiceWorker() {
  if (process.env.NODE_ENV !== 'production') {
    return;
  }

  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker
        .register('/service-worker.js')
        .catch((error) => {
          console.error('Service worker registration failed:', error);
        });
    });
  }
}
