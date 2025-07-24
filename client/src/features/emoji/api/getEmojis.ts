import { api } from '@/app/lib/api';
import { EmojiResponse } from '../type/emoji';

export const getEmojis = async (commentId: number): Promise<EmojiResponse> => {
  const response = await api.get(`/emojis/${commentId}`);
  return response.data;
};
