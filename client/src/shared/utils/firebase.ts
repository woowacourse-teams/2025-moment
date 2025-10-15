import { getApps, initializeApp } from 'firebase/app';
import { getMessaging, getToken, isSupported, Messaging, onMessage } from 'firebase/messaging';

// --- 1) Firebase 앱 초기화
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

// --- 2) Messaging 인스턴스
let messagingPromise: Promise<Messaging | null> | null = null;

export const getMessagingIfSupported = (): Promise<Messaging | null> => {
  if (messagingPromise) return messagingPromise;
  messagingPromise = (async () => {
    if (typeof window === 'undefined') return null;
    const supported = await isSupported().catch(() => false);
    if (!supported) return null;
    return getMessaging(app);
  })();
  return messagingPromise;
};

// --- 3) FCM 토큰 발급
export const requestFCMPermissionAndToken = async (): Promise<string | null> => {
  if (typeof window === 'undefined') return null;

  const permission = await Notification.requestPermission();
  if (permission !== 'granted') {
    console.warn('[FCM] 알림 권한이 거부되었습니다.');
    return null;
  }

  const messaging = await getMessagingIfSupported();
  if (!messaging) {
    console.warn('[FCM] Messaging을 지원하지 않습니다.');
    return null;
  }

  const vapidKey =
    'BB1cSVSj8zJ47p-Nv43yQt70duijJiDScJZWQ2cxFRcAdDV7WCGt5LHxd3Z1wLOeZSWBpXI52kzDj9S-MgppeNw';

  try {
    const registration = await navigator.serviceWorker.ready;

    const token = await getToken(messaging, {
      vapidKey,
      serviceWorkerRegistration: registration,
    });

    if (token) {
      console.log('[FCM] 토큰 발급 성공');
      return token;
    }

    console.warn('[FCM] 토큰을 발급받지 못했습니다.');
    return null;
  } catch (e) {
    console.error('[FCM] 토큰 발급 실패:', e);
    return null;
  }
};

// --- 4) 포그라운드 메시지 리스너 설치
let foregroundUnsubscribe: (() => void) | null = null;

export const setupForegroundMessage = async () => {
  const messaging = await getMessagingIfSupported();
  if (!messaging) return;

  // 이전 리스너가 있다면 정리
  if (foregroundUnsubscribe) {
    foregroundUnsubscribe();
  }

  // 포그라운드 메시지 리스너 등록
  // 알림 표시는 Service Worker가 처리 (포그라운드/백그라운드 일관성 유지)
  foregroundUnsubscribe = onMessage(messaging, () => {
    // Firebase SDK가 포그라운드 메시지를 감지하도록 리스너 등록
    // 실제 알림 표시는 Service Worker의 onBackgroundMessage가 처리
  });
};
