import { useMutation, useQueryClient } from '@tanstack/react-query';
import { sendEmoji } from '../api/sendEmoji';

export const useEmojiMutation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: sendEmoji,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['moments'] });
    },
  });
};
