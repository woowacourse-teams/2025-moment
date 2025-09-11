import { api } from '@/app/lib/api';
import {
  GetCommentableMoments,
  GetCommentableMomentsResponse,
} from '@/features/comment/types/comments';
import { useQuery } from '@tanstack/react-query';

export const useCommentableMomentsQuery = (options?: { enabled?: boolean }) => {
  return useQuery({
    queryKey: ['commentableMoments'],
    queryFn: getCommentableMoments,
    enabled: options?.enabled ?? true,
  });
};

const getCommentableMoments = async (): Promise<GetCommentableMoments> => {
  const response = await api.get<GetCommentableMomentsResponse>('/moments/commentable');
  return response.data.data;
};
