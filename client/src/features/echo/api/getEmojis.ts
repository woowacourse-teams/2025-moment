import { api } from '@/app/lib/api';
import { EchoResponse } from '../type/echos';

export const getEmojis = async (commentId: number): Promise<EchoResponse> => {
  const response = await api.get(`/emojis/${commentId}`);
  return response.data;
};
