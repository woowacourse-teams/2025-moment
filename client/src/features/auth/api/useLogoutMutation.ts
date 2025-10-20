import { api } from '@/app/lib/api';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { flushSync } from 'react-dom';
import { useNavigate } from 'react-router';

export const useLogoutMutation = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: logoutUser,
    onSuccess: () => {
      flushSync(() => {
        queryClient.clear();
      });
      showSuccess('로그아웃 되었습니다.');
      navigate('/login');
    },
    onError: () => {
      showError('로그아웃에 실패했습니다.');
    },
  });
};

const deletePushNotification = async (): Promise<void> => {
  const deviceEndpoint = localStorage.getItem('deviceEndpoint');

  if (!deviceEndpoint) return;

  await api.delete('/push-notifications', {
    data: { deviceEndpoint },
  });
};

const logoutUser = async (): Promise<void> => {
  const [logoutResult, pushResult] = await Promise.allSettled([
    api.post('/auth/logout'),
    deletePushNotification(),
  ]);

  if (logoutResult.status === 'rejected') {
    throw logoutResult.reason;
  }

  if (pushResult.status === 'fulfilled') {
    localStorage.removeItem('deviceEndpoint');
  }

  return logoutResult.value.data;
};
