import { queryClient } from '@/app/lib/queryClient';
import { useToast } from '@/shared/hooks/useToast';
import { useMutation } from '@tanstack/react-query';
import { deleteEmoji } from '../api/deleteEmoji';

export const useDeleteEmojiMutation = () => {
  const { showSuccess, showError } = useToast();

  return useMutation({
    mutationFn: deleteEmoji,
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
