import { useEffect } from 'react';

import * as Sentry from '@sentry/react';

import { isDevice, isPWA } from '../../utils/device';

import { requestFCMPermission, setupForegroundMessage } from './firebase';
import { registerFCMToken } from './registerFCMToken';

export const useInitializeFCM = () => {
  useEffect(() => {
    const handleExpoPushToken = async (token: string) => {
      try {
        await registerFCMToken(token);
        console.log('Registered Expo Push Token from Native:', token);
      } catch (e) {
        console.error('Failed to register Expo Push Token:', e);
      }
    };

    if (typeof window !== 'undefined') {
      (window as any).onExpoPushToken = handleExpoPushToken;
    }

    const initializeFCM = async () => {
      if (typeof window !== 'undefined' && (window as any).ReactNativeWebView) return;

      if (!('serviceWorker' in navigator)) return;

      if (isDevice() && !isPWA()) return;

      try {
        await navigator.serviceWorker.register('/firebase-messaging-sw.js');
        const token = await requestFCMPermission();

        if (token) {
          await setupForegroundMessage();
        }
      } catch (error) {
        // Firebase Messaging이 지원되지 않는 브라우저에서는 무시
        Sentry.captureException(error);
      }
    };

    initializeFCM();
  }, []);
};
