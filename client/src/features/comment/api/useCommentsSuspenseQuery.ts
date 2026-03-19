import { useSuspenseInfiniteQuery } from '@tanstack/react-query';
import { api } from '@/app/lib/api';
import { queryKeys } from '@/shared/lib/queryKeys';
import { CommentsResponse, GetComments } from '../types/comments';

export const useCommentsSuspenseQuery = (groupId: number | string) => {
  const numericGroupId = Number(groupId);
  return useSuspenseInfiniteQuery({
    queryKey: queryKeys.group.comments(numericGroupId),
    queryFn: ({ pageParam }: { pageParam: string | number | null }) =>
      getComments({ groupId: numericGroupId, pageParam }),
    getNextPageParam: lastPage =>
      lastPage.data.hasNextPage ? lastPage.data.nextCursor : undefined,
    initialPageParam: null,
  });
};

export const getComments = async ({
  groupId,
  pageParam,
}: GetComments & { groupId: number | string }): Promise<CommentsResponse> => {
  const params = new URLSearchParams();
  if (pageParam) {
    params.append('nextCursor', String(pageParam));
  }
  params.append('pageSize', '10');

  const response = await api.get<CommentsResponse>(
    `/groups/${groupId}/my-comments?${params.toString()}`,
  );
  return response.data;
};
