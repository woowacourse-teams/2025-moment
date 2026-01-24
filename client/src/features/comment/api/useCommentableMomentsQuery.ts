import { api } from '@/app/lib/api';
import {
  GetCommentableMoments,
  GetCommentableMomentsResponse,
} from '@/features/comment/types/comments';
import { useQuery } from '@tanstack/react-query';

export const useCommentableMomentsQuery = (
  groupId: number | string | undefined,
  options?: { enabled?: boolean },
  tagName?: string,
) => {
  return useQuery({
    queryKey: ['commentableMoments', groupId, tagName],
    queryFn: () => getCommentableMoments(groupId, tagName),
    enabled: !!groupId && (options?.enabled ?? true),
  });
};

const getCommentableMoments = async (
  groupId: number | string | undefined,
  tagName?: string,
): Promise<GetCommentableMoments> => {
  if (!groupId) throw new Error('groupId is required');
  const response = await api.get<GetCommentableMomentsResponse>(
    `/groups/${groupId}/moments/commentable`,
    tagName ? { params: { tagName } } : {},
  );
  return response.data.data;
};
