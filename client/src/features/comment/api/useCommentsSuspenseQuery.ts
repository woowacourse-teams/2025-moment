import { useSuspenseInfiniteQuery } from '@tanstack/react-query';
import { api } from '@/app/lib/api';
import { CommentsResponse, GetComments } from '../types/comments';

export const useCommentsSuspenseQuery = (groupId: number | string) => {
  return useSuspenseInfiniteQuery({
    queryKey: ['group', groupId, 'comments'],
    queryFn: ({ pageParam }: { pageParam: string | null }) => getComments({ groupId, pageParam }),
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
    params.append('nextCursor', pageParam);
  }
  params.append('pageSize', '10');

  const response = await api.get<CommentsResponse>(
    `/groups/${groupId}/comments/me?${params.toString()}`,
  );
  return response.data;
};
