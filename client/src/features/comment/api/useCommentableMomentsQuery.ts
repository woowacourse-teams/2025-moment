import { api } from '@/app/lib/api';
import {
  GetCommentableMoments,
  GetCommentableMomentsResponse,
} from '@/features/comment/types/comments';
import { useQuery } from '@tanstack/react-query';

export const useCommentableMomentsQuery = (options?: { enabled?: boolean }, tagName?: string) => {
  return useQuery({
    queryKey: ['commentableMoments', tagName],
    queryFn: () => getCommentableMoments(tagName),
    enabled: options?.enabled ?? true,
  });
};

const getCommentableMoments = async (tagName?: string): Promise<GetCommentableMoments> => {
  const response = await api.get<GetCommentableMomentsResponse>(
    '/moments/commentable',
    tagName ? { params: { tagName } } : {},
  );
  return response.data.data;
};
