import { useSuspenseInfiniteQuery } from '@tanstack/react-query';
import { api } from '@/app/lib/api';
import { CommentsResponse, GetComments } from '../types/comments';

export const useCommentsSuspenseQuery = () => {
  return useSuspenseInfiniteQuery({
    queryKey: ['comments'],
    queryFn: getComments,
    getNextPageParam: lastPage =>
      lastPage.data.hasNextPage ? lastPage.data.nextCursor : undefined,
    initialPageParam: null,
  });
};

export const getComments = async ({ pageParam }: GetComments): Promise<CommentsResponse> => {
  const params = new URLSearchParams();
  if (pageParam) {
    params.append('nextCursor', pageParam);
  }
  params.append('pageSize', '10');

  const response = await api.get<CommentsResponse>(`/comments/me?${params.toString()}`);
  return response.data;
};
