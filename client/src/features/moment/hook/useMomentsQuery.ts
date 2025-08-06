import { useInfiniteQuery } from '@tanstack/react-query';
import { getMoments } from '../api/getMoments';

export const useMomentsQuery = () => {
  return useInfiniteQuery({
    queryKey: ['moments'],
    queryFn: getMoments,
    getNextPageParam: lastPage =>
      lastPage.data.hasNextPage ? lastPage.data.nextCursor : undefined,
    initialPageParam: null,
  });
};
