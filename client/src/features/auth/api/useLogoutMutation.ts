import { api } from '@/app/lib/api';
import { toast } from '@/shared/store/toast';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { flushSync } from 'react-dom';
import { useNavigate } from 'react-router';

export const useLogoutMutation = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: logoutUser,
    onSuccess: () => {
      flushSync(() => {
        queryClient.clear();
      });
      toast.success('로그아웃 되었습니다.');
      navigate('/login');
    },
    onError: () => {
      toast.error('로그아웃에 실패했습니다.');
    },
  });
};

const logoutUser = async (): Promise<void> => {
  const deviceEndpoint = localStorage.getItem('deviceEndpoint');

  const response = await api.post('/auth/logout', {
    deviceEndpoint,
  });

  localStorage.removeItem('deviceEndpoint');

  return response.data;
};
