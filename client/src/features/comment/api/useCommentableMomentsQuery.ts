import { api } from '@/app/lib/api';
import { queryKeys } from '@/shared/lib/queryKeys';
import { GetCommentableMoments } from '@/features/comment/types/comments';
import { useQuery } from '@tanstack/react-query';

export interface GetCommentableMomentsResponse {
  status: number;
  data: GetCommentableMoments;
}

export const useCommentableMomentsQuery = (
  groupId: number | string | undefined,
  options?: { enabled?: boolean },
) => {
  return useQuery({
    queryKey: queryKeys.commentableMoments.byGroup(Number(groupId)),
    queryFn: () => getCommentableMoments(groupId),
    enabled: !!groupId && (options?.enabled ?? true),
  });
};

const getCommentableMoments = async (
  groupId: number | string | undefined,
): Promise<GetCommentableMoments> => {
  if (!groupId) throw new Error('groupId is required');
  const response = await api.get<GetCommentableMomentsResponse>(
    `/groups/${groupId}/moments/commentable`,
  );
  return response.data.data;
};
