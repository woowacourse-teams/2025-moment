import { api } from '@/app/lib/api';
import { EchoResponse } from '../type/echos';
import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';

export const useDeleteEmojiMutation = () => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: deleteEcho,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['moments'] });
      showSuccess('이모지를 제거했습니다!');
    },
    onError: () => {
      const errorMessage = '이모지 제거에 실패했습니다. 다시 시도해주세요.';
      showError(errorMessage);
    },
  });
};

const deleteEcho = async (echoId: number): Promise<EchoResponse> => {
  const response = await api.delete(`/echos/${echoId}`);
  return response.data;
};
