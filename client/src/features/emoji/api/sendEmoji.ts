import { api } from '@/app/lib/api';
import { EmojiRequest, EmojiResponse } from '@/features/emoji/type/emoji';

export const sendEmoji = async (emoji: EmojiRequest): Promise<EmojiResponse> => {
  const response = await api.post('/emojis', { emoji });
  return response.data;
};
