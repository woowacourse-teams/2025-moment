import { useEffect } from 'react';

import { toast } from '@/shared/store/toast';

import { registerPushToken } from './registerPushToken';

interface PushNotificationData {
  title?: string;
  body?: string;
  data?: Record<string, unknown>;
}

export const useInitializePushNotification = () => {

  useEffect(() => {
    // Native 앱에서 Expo 푸시 토큰 수신 시 서버에 등록
    const handleExpoPushToken = async (token: string) => {
      try {
        await registerPushToken(token);
        console.log('Registered Expo Push Token from Native:', token);
      } catch (e) {
        console.error('Failed to register Expo Push Token:', e);
      }
    };

    // 포그라운드에서 푸시 알림 수신 시 인앱 UI로 표시
    const handlePushNotification = (data: PushNotificationData) => {
      console.log('Push notification received (foreground):', data);
      if (data.body) {
        toast.message(data.body, 'moment', 5000);
      }
    };

    if (typeof window !== 'undefined') {
      (window as any).onExpoPushToken = handleExpoPushToken;
      (window as any).onPushNotification = handlePushNotification;
    }
  }, []);
};
