import { api } from '@/app/lib/api';
import { queryClient } from '@/app/lib/queryClient';
import { getProfile } from '@/features/auth/api/useProfileQuery';
import { useToast } from '@/shared/hooks/useToast';
import { requestFCMPermission } from '@/shared/lib/notifications/firebase';
import { registerFCMToken } from '@/shared/lib/notifications/registerFCMToken';
import { useMutation } from '@tanstack/react-query';
import { isAxiosError } from 'axios';
import { useNavigate } from 'react-router';
import { AppleLoginRequest, AppleLoginResponse } from '../types/appleLogin';

export const useAppleLoginMutation = () => {
  const navigate = useNavigate();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: appleLogin,
    onSuccess: async () => {
      queryClient.setQueryData(['checkIfLoggedIn'], true);
      await queryClient.prefetchQuery({ queryKey: ['profile'], queryFn: getProfile });

      try {
        const token = await requestFCMPermission();
        if (token) {
          await registerFCMToken(token);
        }
      } catch (error) {
        console.error('FCM 토큰 등록 실패:', error);
      }

      showSuccess('Apple 로그인에 성공했습니다!');
      navigate('/');
    },
    onError: error => {
      if (isAxiosError(error) && error.response?.data.message) {
        showError(error.response.data.message);
      } else {
        showError('Apple 로그인에 실패했습니다. 다시 시도해주세요.');
      }
    },
  });
};

const appleLogin = async (data: AppleLoginRequest): Promise<AppleLoginResponse> => {
  const response = await api.post('/auth/apple', data);
  return response.data;
};
