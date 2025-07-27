import { useMutation } from '@tanstack/react-query';
import { deleteEmoji } from '../api/deleteEmoji';
import { queryClient } from '@/app/lib/queryClient';

export const useDeleteEmojiMutation = () => {
  return useMutation({
    mutationFn: deleteEmoji,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['moments'] });
    },
  });
};
