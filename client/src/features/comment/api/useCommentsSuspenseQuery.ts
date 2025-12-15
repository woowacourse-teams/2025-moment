import { useSuspenseInfiniteQuery } from '@tanstack/react-query';
import { getComments } from './getComments';

export const useCommentsSuspenseQuery = () => {
  return useSuspenseInfiniteQuery({
    queryKey: ['comments'],
    queryFn: getComments,
    getNextPageParam: lastPage =>
      lastPage.data.hasNextPage ? lastPage.data.nextCursor : undefined,
    initialPageParam: null,
  });
};
