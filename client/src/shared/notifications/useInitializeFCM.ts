import { useEffect } from 'react';

import * as Sentry from '@sentry/react';

import { isDevice, isPWA } from '../utils/device';

import { requestFCMPermission, setupForegroundMessage } from './firebase';

export const useInitializeFCM = () => {
  useEffect(() => {
    const initializeFCM = async () => {
      if (!('serviceWorker' in navigator) || (isDevice() && !isPWA())) return;

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
