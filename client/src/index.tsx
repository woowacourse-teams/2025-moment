import App from '@/app/App';
import { createRoot } from 'react-dom/client';
import '../instrument';

async function registerServiceWorker() {
  if (!('serviceWorker' in navigator)) {
    console.warn('[SW] Service Worker를 지원하지 않는 브라우저입니다.');
    return null;
  }

  // MSW Service Worker 제거 (프로덕션 환경에서 불필요)
  try {
    const registrations = await navigator.serviceWorker.getRegistrations();
    for (const registration of registrations) {
      if (registration.active?.scriptURL.includes('mockServiceWorker')) {
        await registration.unregister();
      }
    }
  } catch (error) {
    console.warn('[SW] MSW 제거 실패:', error);
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
