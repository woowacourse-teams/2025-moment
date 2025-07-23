import { api } from '@/app/lib/api';

export const deleteEmoji = async (emojiId: number) => {
  const response = await api.delete(`/emojis/${emojiId}`);
  return response.data;
};
