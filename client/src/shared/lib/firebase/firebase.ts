import { getApps, initializeApp } from 'firebase/app';
import { getMessaging, getToken, isSupported, Messaging, onMessage } from 'firebase/messaging';

// --- 1) Firebase 앱 초기화 (중복 방지)
const firebaseConfig = {
  apiKey: 'AIzaSyD4qy5-cB5BclCX36uoyWx0RjOs2vZ-i1c',
  authDomain: 'moment-8787a.firebaseapp.com',
  projectId: 'moment-8787a',
  storageBucket: 'moment-8787a.firebasestorage.app',
  messagingSenderId: '138468882061',
  appId: '1:138468882061:web:d2b1ee112d4e98a322d4c4',
  measurementId: 'G-NM044KCWN9',
};

const app = getApps().length ? getApps()[0] : initializeApp(firebaseConfig);

// --- 2) Messaging 인스턴스 (브라우저 지원 체크)
let messagingPromise: Promise<Messaging | null> | null = null;

export const getMessagingIfSupported = (): Promise<Messaging | null> => {
  if (messagingPromise) return messagingPromise;
  messagingPromise = (async () => {
    if (typeof window === 'undefined') return null; // SSR 보호
    const supported = await isSupported().catch(() => false);
    if (!supported) return null;
    return getMessaging(app);
  })();
  return messagingPromise;
};

// --- 3) FCM 토큰 발급 (권한 요청 포함)
export const requestFCMPermissionAndToken = async (): Promise<string | null> => {
  if (typeof window === 'undefined') return null;

  const permission = await Notification.requestPermission();
  if (permission !== 'granted') return null;

  const messaging = await getMessagingIfSupported();
  if (!messaging) return null;

  // VAPID 공개키는 '웹 푸시 인증서'의 키를 사용
  const vapidKey =
    'BB1cSVSj8zJ47p-Nv43yQt70duijJiDScJZWQ2cxFRcAdDV7WCGt5LHxd3Z1wLOeZSWBpXI52kzDj9S-MgppeNw';

  try {
    const token = await getToken(messaging, {
      vapidKey,
      serviceWorkerRegistration: await navigator.serviceWorker.getRegistration(), // 이미 등록된 SW 재사용
    });
    return token ?? null;
  } catch (e) {
    console.error('[FCM] getToken 실패:', e);
    return null;
  }
};

// --- 4) 포그라운드 메시지 리스너 설치
export const setupForegroundMessage = async (onNotify?: (payload: any) => void) => {
  const messaging = await getMessagingIfSupported();
  if (!messaging) return;

  onMessage(messaging, payload => {
    // 콜백 제공 시 앱 UI로 전달
    onNotify?.(payload);

    // 시스템 알림 사용 (선택)
    if (Notification.permission === 'granted') {
      new Notification(payload.notification?.title ?? '새 알림', {
        body: payload.notification?.body ?? '',
        icon: '/icon-192x192.png',
        data: payload.data,
      });
    }
  });
};
