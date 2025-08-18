import { useMutation, useQueryClient } from '@tanstack/react-query';
import { sendEcho } from '../api/sendEcho';
import { useToast } from '@/shared/hooks/useToast';

export const useEchoMutation = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: sendEcho,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['moments'] });
      showSuccess('에코가 성공적으로 전송되었습니다! 별조각 3개를 얻으셨습니다.');
    },
    onError: () => {
      showError('에코 전송에 실패했습니다. 다시 시도해주세요.');
    },
  });
};
