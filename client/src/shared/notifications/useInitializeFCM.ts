import { useEffect } from 'react';

import * as Sentry from '@sentry/react';

import { isIOS } from '../utils/device';

import { requestFCMPermission, setupForegroundMessage } from './firebase';
import { api } from '@/app/lib/api';
import { useCheckIfLoggedInQuery } from '@/features/auth/api/useCheckIfLoggedInQuery';

export const registerFCMToken = async (registrationToken: string) => {
  return await api.post<void>('/push-notifications', { deviceEndpoint: registrationToken });
};

export const useInitializeFCM = () => {
  const { data: isLoggedIn } = useCheckIfLoggedInQuery();

  useEffect(() => {
    const initializeFCM = async () => {
      if (!('serviceWorker' in navigator) || isIOS()) return;

      try {
        await navigator.serviceWorker.register('/firebase-messaging-sw.js');
        const token = await requestFCMPermission();

        if (!token || !isLoggedIn) return;

        await registerFCMToken(token);
        setupForegroundMessage();
      } catch (error) {
        // TODO : FCM 초기화 실패 시 처리 로직 추가
        Sentry.captureException(error);
      }
    };

    initializeFCM();
  }, []);
};
