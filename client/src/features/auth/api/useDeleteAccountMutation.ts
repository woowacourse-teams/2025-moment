import { api } from '@/app/lib/api';
import { toast } from '@/shared/store/toast';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { isAxiosError } from 'axios';
import { flushSync } from 'react-dom';
import { useNavigate } from 'react-router';

export const useDeleteAccountMutation = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: deleteAccount,
    onSuccess: () => {
      flushSync(() => {
        queryClient.clear();
      });
      localStorage.removeItem('deviceEndpoint');
      toast.success('회원 탈퇴가 완료되었습니다.');
      navigate('/login');
    },
    onError: error => {
      if (isAxiosError(error) && error.response?.data?.message) {
        toast.error(error.response.data.message);
      } else {
        toast.error('회원 탈퇴에 실패했습니다. 다시 시도해주세요.');
      }
    },
  });
};

const deleteAccount = async (): Promise<void> => {
  await api.delete('/me');
};
