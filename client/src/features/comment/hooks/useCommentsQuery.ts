import { useInfiniteQuery } from '@tanstack/react-query';
import { getComments } from '../api/getComments';

export const useCommentsQuery = () => {
  return useInfiniteQuery({
    queryKey: ['comments'],
    queryFn: getComments,
    getNextPageParam: lastPage =>
      lastPage.data.hasNextPage ? lastPage.data.nextCursor : undefined,
    initialPageParam: null,
  });
};
