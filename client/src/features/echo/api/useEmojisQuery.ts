import { api } from '@/app/lib/api';
import { EchoResponse } from '../type/echos';
import { useQuery } from '@tanstack/react-query';

export const useEmojisQuery = (commentId: number) => {
  return useQuery({
    queryKey: ['emojis', commentId],
    queryFn: () => getEmojis(commentId),
  });
};

const getEmojis = async (commentId: number): Promise<EchoResponse> => {
  const response = await api.get(`/emojis/${commentId}`);
  return response.data;
};
