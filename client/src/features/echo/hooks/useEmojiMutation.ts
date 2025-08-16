import { useMutation, useQueryClient } from '@tanstack/react-query';
import { sendEmoji } from '../api/sendEmoji';
import { useToast } from '@/shared/hooks/useToast';

export const useEmojiMutation = () => {
  const queryClient = useQueryClient();
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: sendEmoji,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['moments'] });
      showSuccess('이모지를 추가했습니다!');
    },
    onError: () => {
      const errorMessage = '이모지 추가에 실패했습니다. 다시 시도해주세요.';
      showError(errorMessage);
    },
  });
};
