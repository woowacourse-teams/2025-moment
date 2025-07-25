import { api } from '@/app/lib/api';
import { EmojiResponse } from '../type/emoji';

export const deleteEmoji = async (emojiId: number): Promise<EmojiResponse> => {
  const response = await api.delete(`/emojis/${emojiId}`);
  return response.data;
};
