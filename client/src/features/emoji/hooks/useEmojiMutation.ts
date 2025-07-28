import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { sendEmoji } from '../api/sendEmoji';

export const useEmojiMutation = () => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: sendEmoji,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['emojis'] });
      showSuccess('이모지를 추가했습니다!');
    },
    onError: () => {
      const errorMessage = '이모지 추가에 실패했습니다. 다시 시도해주세요.';
      showError(errorMessage);
    },
  });
};
