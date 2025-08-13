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

window.addEventListener('beforeinstallprompt', e => {
  console.log('[PWA] beforeinstallprompt 이벤트 발생');
  // 기본 브라우저 설치 프롬프트 방지
  e.preventDefault();
  // window 객체에 저장하여 전역에서 접근 가능하게 함
  (window as any).deferredPrompt = e;

  // 설치 가능 상태임을 확인
  console.log('[PWA] 설치 가능 상태');
});

// MSW랑 같이 켜지않도록 주의
if ('serviceWorker' in navigator) {
  window.addEventListener('load', async () => {
    try {
      const reg = await navigator.serviceWorker.register('/firebase-messaging-sw.js');
      await navigator.serviceWorker.ready; // 등록 완료 보장
      console.log('[SW] ready:', reg.scope);

      const { requestFCMPermissionAndToken, setupForegroundMessage } = await import(
        '@/shared/lib/firebase/firebase'
      );
      const token = await requestFCMPermissionAndToken(); // 내부에서 vapidKey 사용
      console.log('[FCM] token:', token);
      if (token) {
        localStorage.setItem('fcmToken', token); // (선택) 나중에 쉽게 꺼내보려고 저장
        await setupForegroundMessage(); // 포그라운드 알림 리스너 설습
      }
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
