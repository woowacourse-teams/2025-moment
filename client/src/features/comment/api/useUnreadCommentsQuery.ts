import { useInfiniteQuery } from '@tanstack/react-query';
import { GetUnreadComments, UnreadCommentsResponse } from '../types/comments';
import { api } from '@/app/lib/api';

export const useUnreadCommentsQuery = () => {
  return useInfiniteQuery<UnreadCommentsResponse>({
    queryKey: ['comments', 'unread'],
    queryFn: ({ pageParam }) => getUnreadComments({ pageParam: pageParam as string | null }),
    getNextPageParam: lastPage =>
      lastPage.data.hasNextPage ? lastPage.data.nextCursor : undefined,
    initialPageParam: null,
  });
};

const getUnreadComments = async ({
  pageParam,
}: GetUnreadComments): Promise<UnreadCommentsResponse> => {
  const params = new URLSearchParams();
  if (pageParam) {
    params.append('nextCursor', pageParam);
  }
  params.append('pageSize', '10');

  const response = await api.get(`/comments/me/unread?${params.toString()}`);
  return response.data;
};
