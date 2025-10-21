import { useEffect } from 'react';

import * as Sentry from '@sentry/react';

import { isIOS } from '../utils/device';

import { requestFCMPermission, setupForegroundMessage } from './firebase';

export const useInitializeFCM = () => {
  useEffect(() => {
    const initializeFCM = async () => {
      if (!('serviceWorker' in navigator) || isIOS()) return;

      try {
        await navigator.serviceWorker.register('/firebase-messaging-sw.js');
        const token = await requestFCMPermission();

        if (token) {
          setupForegroundMessage();
        }
      } catch (error) {
        // TODO : FCM 초기화 실패 시 처리 로직 추가
        Sentry.captureException(error);
      }
    };

    initializeFCM();
  }, []);
};
