import { useMutation } from '@tanstack/react-query';
import { sendEmoji } from '../api/sendEmoji';

export const useEmojiMutation = () => {
  return useMutation({
    mutationFn: sendEmoji,
  });
};
