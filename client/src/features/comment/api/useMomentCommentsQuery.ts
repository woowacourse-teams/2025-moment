import { api } from '@/app/lib/api';
import { useQuery } from '@tanstack/react-query';
import { CommentsResponse } from '../types/comments';

export const useMomentCommentsQuery = (groupId: number | string, momentId: number) => {
  return useQuery({
    queryKey: ['group', groupId, 'moment', momentId, 'comments'],
    queryFn: async () => {
      const response = await api.get<CommentsResponse>(
        `/groups/${groupId}/moments/${momentId}/comments`,
      );
      return response.data;
    },
  });
};
