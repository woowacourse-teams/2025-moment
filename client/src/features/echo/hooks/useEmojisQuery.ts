import { useQuery } from '@tanstack/react-query';
import { getEmojis } from '../api/getEmojis';

export const useEmojisQuery = (commentId: number) => {
  return useQuery({
    queryKey: ['emojis', commentId],
    queryFn: () => getEmojis(commentId),
  });
};
