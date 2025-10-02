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

if ('serviceWorker' in navigator) {
  navigator.serviceWorker
    .register('/firebase-messaging-sw.js')
    .then(registration => {
      console.log('[SW] 등록 성공:', registration.scope);
    })
    .catch(error => {
      console.error('[SW] 등록 실패:', error);
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
