import { useSuspenseInfiniteQuery } from '@tanstack/react-query';
import { api } from '@/app/lib/api';
import { CommentsResponse, GetComments } from '../types/comments';

export const useUnreadCommentsSuspenseQuery = (groupId: number | string) => {
  return useSuspenseInfiniteQuery({
    queryKey: ['group', groupId, 'comments', 'unread'],
    queryFn: ({ pageParam }: { pageParam: string | null }) =>
      getUnreadComments({ groupId, pageParam }),
    getNextPageParam: lastPage =>
      lastPage.data.hasNextPage ? lastPage.data.nextCursor : undefined,
    initialPageParam: null,
  });
};

const getUnreadComments = async ({
  groupId,
  pageParam,
}: GetComments & { groupId: number | string }): Promise<CommentsResponse> => {
  const params = new URLSearchParams();
  if (pageParam) {
    params.append('nextCursor', pageParam);
  }
  params.append('pageSize', '10');

  const response = await api.get(`/groups/${groupId}/comments/me/unread?${params.toString()}`);
  return response.data;
};
