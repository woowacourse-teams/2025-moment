import * as Sentry from '@sentry/react';
import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, onMessage } from 'firebase/messaging';

const firebaseConfig = {
  apiKey: 'AIzaSyD4qy5-cB5BclCX36uoyWx0RjOs2vZ-i1c',
  authDomain: 'moment-8787a.firebaseapp.com',
  projectId: 'moment-8787a',
  storageBucket: 'moment-8787a.firebasestorage.app',
  messagingSenderId: '138468882061',
  appId: '1:138468882061:web:d2b1ee112d4e98a322d4c4',
  measurementId: 'G-NM044KCWN9',
};

const app = initializeApp(firebaseConfig);
export const messaging = getMessaging(app);

export const requestFCMPermission = async () => {
  try {
    const permission = await Notification.requestPermission();
    if (permission === 'granted') {
      return await getToken(messaging, {
        vapidKey: process.env.FCM_VAPID_KEY,
      });
    }

    if (permission === 'denied') {
      // alert('알림 권한이 거부되었습니다.');
      return null;
    }

    if (permission === 'default') {
      // alert('알림 권한 선택을 하지 않았습니다.');
      return null;
    }
  } catch (error) {
    // TODO : FCM 토큰 획득 실패 시 처리 로직 추가
    Sentry.captureException(error);
    return null;
  }
};

export const setupForegroundMessage = () => {
  onMessage(messaging, payload => {
    if (Notification.permission === 'granted' && payload.notification) {
      const notification = new Notification(payload.notification?.title || '새 알림', {
        body: payload.notification?.body || '',
        icon: '/icon-192x192.png',
        data: payload.data,
      });

      notification.onclick = event => {
        const data = (event.target as Notification).data;
        const redirectUrl = data?.redirectUrl;

        if (redirectUrl) {
          window.location.href = redirectUrl;
        }
        notification.close();
      };
    }
  });
};
