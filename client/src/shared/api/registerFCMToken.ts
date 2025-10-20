import { api } from '@/app/lib/api';
import { useMutation } from '@tanstack/react-query';

export const registerFCMToken = async (fcmToken: string): Promise<void> => {
  await api.post('/push-notifications', {
    deviceEndpoint: fcmToken,
  });

  const deviceEndpoint = fcmToken;
  localStorage.setItem('deviceEndpoint', deviceEndpoint);
};

export const useRegisterFCMToken = () => {
  return useMutation({
    mutationFn: registerFCMToken,
    onError: error => {
      console.error('FCM 토큰 등록 실패:', error);
    },
  });
};
