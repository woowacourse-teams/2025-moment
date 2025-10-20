import App from '@/app/App';
import { createRoot } from 'react-dom/client';
import '../instrument';

async function registerServiceWorker() {
  if (!('serviceWorker' in navigator)) {
    console.warn('[SW] Service Worker를 지원하지 않는 브라우저입니다.');
    return null;
  }

  try {
    const registration = await navigator.serviceWorker.register('/firebase-messaging-sw.js', {
      scope: '/',
    });

    // Service Worker가 활성화될 때까지 대기
    if (registration.installing) {
      await new Promise<void>(resolve => {
        registration.installing!.addEventListener('statechange', function () {
          if (this.state === 'activated') {
            resolve();
          }
        });
      });
    } else {
      await navigator.serviceWorker.ready;
    }

    console.log('[SW] Service Worker 등록 완료');
    return registration;
  } catch (error) {
    console.error('[SW] 등록 실패:', error);
    return null;
  }
}

async function startApp() {
  await registerServiceWorker();

  const rootElement = document.getElementById('root');
  if (!rootElement) {
    throw new Error('Root element not found');
  }

  const root = createRoot(rootElement);
  root.render(<App />);
}

startApp().catch(console.error);
