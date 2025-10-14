import { api } from '@/app/lib/api';
import { useMutation } from '@tanstack/react-query';

export const registerFCMToken = async (fcmToken: string): Promise<void> => {
  const response = await api.post('/push-notifications', { deviceEndPoint: fcmToken });
  return response.data;
};

export const useRegisterFCMToken = () => {
  return useMutation({
    mutationFn: registerFCMToken,
    onError: error => {
      console.error('FCM 토큰 등록 실패:', error);
    },
  });
};
