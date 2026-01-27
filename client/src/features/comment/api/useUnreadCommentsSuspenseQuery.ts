import { useSuspenseInfiniteQuery } from '@tanstack/react-query';
import { api } from '@/app/lib/api';
import { CommentsResponse, GetComments } from '../types/comments';

export const useUnreadCommentsSuspenseQuery = (groupId: number | string) => {
  const numericGroupId = Number(groupId);
  return useSuspenseInfiniteQuery({
    queryKey: ['group', numericGroupId, 'comments', 'unread'],
    queryFn: ({ pageParam }: { pageParam: string | number | null }) =>
      getUnreadComments({ groupId: numericGroupId, pageParam }),
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
    params.append('nextCursor', String(pageParam));
  }
  params.append('pageSize', '10');

  const response = await api.get(`/groups/${groupId}/my-comments/unread?${params.toString()}`);
  return response.data;
};
