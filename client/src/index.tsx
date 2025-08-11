import App from '@/app/App';
import { createRoot } from 'react-dom/client';
import '../instrument';

async function enableMocking() {
  if (process.env.NODE_ENV !== 'development') {
    return;
  }

  const { worker } = await import('./mocks/browser');

  return worker.start({
    onUnhandledRequest: 'warn',
  });
}

// MSW랑 같이 켜지않도록 주의
if ('serviceWorker' in navigator) {
  window.addEventListener('load', async () => {
    try {
      const reg = await navigator.serviceWorker.register('/firebase-messaging-sw.js');
      await navigator.serviceWorker.ready; // 등록 완료 보장
      console.log('[SW] ready:', reg.scope);
    } catch (e) {
      console.error('[SW] registration failed:', e);
    }
  });
}

async function startApp() {
  // await enableMocking();
  // cd

  const rootElement = document.getElementById('root');
  if (!rootElement) {
    throw new Error('Root element not found');
  }

  const root = createRoot(rootElement);
  root.render(<App />);
}

startApp().catch(console.error);
