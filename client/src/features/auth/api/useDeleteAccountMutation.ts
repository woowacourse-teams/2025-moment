import { api } from '@/app/lib/api';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { flushSync } from 'react-dom';
import { useNavigate } from 'react-router';

export const useDeleteAccountMutation = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: deleteAccount,
    onSuccess: () => {
      flushSync(() => {
        queryClient.clear();
      });
      localStorage.removeItem('deviceEndpoint');
      showSuccess('회원 탈퇴가 완료되었습니다.');
      navigate('/login');
    },
    onError: () => {
      showError('회원 탈퇴에 실패했습니다. 다시 시도해주세요.');
    },
  });
};

const deleteAccount = async (): Promise<void> => {
  await api.delete('/me');
};
