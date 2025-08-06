import { useInfiniteQuery } from '@tanstack/react-query';
import { getMoments } from '../api/getMoments';

export const useMomentsQuery = () => {
  return useInfiniteQuery({
    queryKey: ['moments'],
    queryFn: ({ pageParam }: { pageParam: string | null }) => getMoments({ pageParam }),
    getNextPageParam: lastPage =>
      lastPage.data.hasNextPage ? lastPage.data.nextCursor : undefined,
    initialPageParam: null,
  });
};
